package main

import (
	"crypto"
	"crypto/aes"
	"crypto/cipher"
	"crypto/rand"
	"crypto/rsa"
	"crypto/sha256"
	"crypto/x509"
	"encoding/base64"
	"syscall/js"
)

func main() {
	c := make(chan bool)
	js.Global().Set("wasmDecryptToString", js.FuncOf(jsDecryptToString))
	js.Global().Set("wasmDecryptToBase64", js.FuncOf(jsDecryptToBase64))
	js.Global().Set("wasmEncryptString", js.FuncOf(jsEncryptString))
	js.Global().Set("wasmEncrypt", js.FuncOf(jsEncrypt))
	js.Global().Set("wasmCreateKey", js.FuncOf(jsCreateKey))
	js.Global().Set("wasmEncryptAES", js.FuncOf(jsEncryptAES))
	js.Global().Set("wasmDecryptAES", js.FuncOf(jsDecryptAES))
	js.Global().Set("wasmSignFile", js.FuncOf(jsSign))
	<-c
}

func jsSign(this js.Value, args []js.Value) interface{} {
	input := args[0].String()

	rsaPrivateKey := getRsaKey()
	hashed := sha256.Sum256([]byte(input))
	signature, _ := rsa.SignPKCS1v15(rand.Reader, rsaPrivateKey, crypto.SHA256, hashed[:])
	return base64.StdEncoding.EncodeToString(signature)
}

func jsCreateKey(this js.Value, args []js.Value) interface{} {
	getRsaKey()
	return nil
}

func jsEncryptAES(this js.Value, args []js.Value) interface{} {
	secretMessage := args[0].String()
	password := args[1].String()
	return encryptAES(secretMessage, password)
}

func jsDecryptAES(this js.Value, args []js.Value) interface{} {
	secretMessage := args[0]
	password := args[1].String()
	return decryptAES(secretMessage, password)
}
func decryptAES(input js.Value, password string) string {
	ciphertext, _ := base64.StdEncoding.DecodeString(input.Get("data").String())
	salt, _ := base64.StdEncoding.DecodeString(input.Get("salt").String())
	nonce, _ := base64.StdEncoding.DecodeString(input.Get("nonce").String())
	sha := sha256.New()
	sha.Write(salt)
	aesKey := sha.Sum([]byte(password))[:16]

	block, _ := aes.NewCipher(aesKey)
	aesGCM, _ := cipher.NewGCM(block)
	plain, _ := aesGCM.Open(nil, nonce, ciphertext, nil)
	return string(plain)
}

func encryptAES(secretMessage string, password string) map[string]interface{} {
	nonce := make([]byte, 12)
	rand.Read(nonce)
	salt := make([]byte, 12)
	rand.Read(salt)

	sha := sha256.New()
	sha.Write(salt)
	aesKey := sha.Sum([]byte(password))[:16]
	block, _ := aes.NewCipher(aesKey)
	aesGCM, _ := cipher.NewGCM(block)
	ciphertext := aesGCM.Seal(nil, nonce, []byte(secretMessage), nil)

	return map[string]interface{}{
		"data":  base64.StdEncoding.EncodeToString(ciphertext),
		"nonce": base64.StdEncoding.EncodeToString(nonce),
		"salt":  base64.StdEncoding.EncodeToString(salt),
	}
}

func encryptData(secretMessage []byte) map[string]interface{} {
	rsaKey := getRsaKey()
	aesKey := make([]byte, 16)
	nonce := make([]byte, 12)
	rand.Read(aesKey)
	rand.Read(nonce)
	block, _ := aes.NewCipher(aesKey)
	aesGCM, _ := cipher.NewGCM(block)
	ciphertext := aesGCM.Seal(nil, nonce, secretMessage, nil)

	ciphertextKey, _ := rsa.EncryptOAEP(sha256.New(), rand.Reader, &rsaKey.PublicKey, aesKey, []byte{})

	return map[string]interface{}{
		"data":  base64.StdEncoding.EncodeToString(ciphertext),
		"key":   base64.StdEncoding.EncodeToString(ciphertextKey),
		"nonce": base64.StdEncoding.EncodeToString(nonce),
	}
}

func jsEncryptString(this js.Value, args []js.Value) interface{} {
	secretMessage := []byte(args[0].String())
	return encryptData(secretMessage)
}

func jsEncrypt(this js.Value, args []js.Value) interface{} {
	input := args[0]
	size := input.Length()
	secretMessage := make([]byte, size)
	js.CopyBytesToGo(secretMessage, input)
	return encryptData(secretMessage)
}

func decrypt(input js.Value) []byte {
	ciphertext, _ := base64.StdEncoding.DecodeString(input.Get("data").String())
	ciphertextKey, _ := base64.StdEncoding.DecodeString(input.Get("key").String())
	nonce, _ := base64.StdEncoding.DecodeString(input.Get("nonce").String())
	rsaKey := getRsaKey()
	aesKey, err := rsa.DecryptOAEP(sha256.New(), rand.Reader, rsaKey, ciphertextKey, []byte{})
	if err != nil {
		return nil
	}
	block, _ := aes.NewCipher(aesKey)
	aesGCM, err := cipher.NewGCM(block)
	if err != nil {
		return nil
	}
	plain, err := aesGCM.Open(nil, nonce, ciphertext, nil)
	if err != nil {
		return nil
	}
	return plain
}

func jsDecryptToString(this js.Value, args []js.Value) interface{} {
	return string(decrypt(args[0]))
}

func jsDecryptToBase64(this js.Value, args []js.Value) interface{} {
	return base64.StdEncoding.EncodeToString(decrypt(args[0]))
}

func getRsaKey() *rsa.PrivateKey {
	key := retrieve("pk")
	if key != nil {
		data, _ := base64.StdEncoding.DecodeString(*key)
		pk, _ := x509.ParsePKCS1PrivateKey(data)
		return pk
	}
	rsaKey, _ := rsa.GenerateKey(rand.Reader, 2048)
	pkData := x509.MarshalPKCS1PrivateKey(rsaKey)
	pkBase64 := base64.StdEncoding.EncodeToString(pkData)
	pubData := x509.MarshalPKCS1PublicKey(&rsaKey.PublicKey)
	pubBase64 := base64.StdEncoding.EncodeToString(pubData)
	store("pub", pubBase64)
	store("pk", pkBase64)
	return rsaKey
}

func store(key string, val string) {
	js.Global().Get("localStorage").Call("setItem", key, val)
}

func retrieve(key string) *string {
	val := js.Global().Get("localStorage").Call("getItem", key)
	if val.Truthy() {
		value := val.String()
		return &value
	}
	return nil
}

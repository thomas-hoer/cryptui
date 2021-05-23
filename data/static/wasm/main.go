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
	"fmt"
	"syscall/js"
)

var wasm js.Value

func main() {
	c := make(chan bool)
	js.Global().Set("wasm", js.FuncOf(jsExecute))
	<-c
}

func jsExecute(this js.Value, args []js.Value) interface{} {
	if len(args) != 1 {
		return nil
	}
	arg := args[0]
	if arg.Type() != js.TypeObject {
		return nil
	}
	defer func() {
		// The program should crash if a panic occures, however this does not replace a proper error handling.
		if r := recover(); r != nil {
			arg.Set("msg", fmt.Sprint(r))
			fmt.Println("Recovered from panic", r)
		}
	}()
	function := arg.Get("function").String()
	switch function {
	case "sign":
		sign(arg)
	case "decryptToString":
		decryptToString(arg)
	case "decryptToBase64":
		decryptToBase64(arg)
	case "encryptArray":
		encryptArray(arg)
	case "encryptString":
		encryptString(arg)
	case "createKey":
		createKey(arg)
	case "encryptAES":
		encryptAES(arg)
	case "decryptAES":
		decryptAES(arg)
	default:
		fmt.Println("Unknown function", function)
	}
	return arg
}

func encryptArray(arg js.Value) {
	input := arg.Get("plain")
	size := input.Length()
	secretMessage := make([]byte, size)
	js.CopyBytesToGo(secretMessage, input)
	key, err := getKey(arg.Get("key").String())
	if err != nil {
		arg.Set("msg", err.Error())
		return
	}
	data, datakey, nonce := encryptData(secretMessage, key)
	arg.Set("data", data)
	arg.Set("datakey", datakey)
	arg.Set("nonce", nonce)
}

func encryptString(arg js.Value) {
	secretMessage := arg.Get("plain").String()
	key, err := getKey(arg.Get("key").String())
	if err != nil {
		arg.Set("msg", err.Error())
		return
	}
	data, datakey, nonce := encryptData([]byte(secretMessage), key)
	arg.Set("data", data)
	arg.Set("datakey", datakey)
	arg.Set("nonce", nonce)
}
func getKey(input string) (*rsa.PrivateKey, error) {
	data, err := base64.StdEncoding.DecodeString(input)
	if err != nil {
		return nil, err
	}
	pk, err := x509.ParsePKCS1PrivateKey(data)
	if err != nil {
		return nil, err
	}
	return pk, nil
}

func decryptToString(arg js.Value) {
	key, err := getKey(arg.Get("key").String())
	if err != nil {
		arg.Set("msg", err.Error())
		return
	}
	arg.Set("plain", string(decryptRSA(arg.Get("data"), key)))
}

func decryptToBase64(arg js.Value) {
	key, err := getKey(arg.Get("key").String())
	if err != nil {
		arg.Set("msg", err.Error())
		return
	}
	arg.Set("base64", base64.StdEncoding.EncodeToString(decryptRSA(arg.Get("data"), key)))
}

func sign(arg js.Value) {
	key, err := getKey(arg.Get("key").String())
	if err != nil {
		arg.Set("msg", err.Error())
		return
	}
	data := arg.Get("data").String()
	hashed := sha256.Sum256([]byte(data))
	signature, err := rsa.SignPKCS1v15(rand.Reader, key, crypto.SHA256, hashed[:])
	if err != nil {
		arg.Set("msg", err.Error())
	} else {
		sign := base64.StdEncoding.EncodeToString(signature)
		arg.Set("sign", sign)
	}
}

func createKey(arg js.Value) {
	rsaKey, _ := rsa.GenerateKey(rand.Reader, 2048)
	pkData := x509.MarshalPKCS1PrivateKey(rsaKey)
	pkBase64 := base64.StdEncoding.EncodeToString(pkData)
	pubData := x509.MarshalPKCS1PublicKey(&rsaKey.PublicKey)
	pubBase64 := base64.StdEncoding.EncodeToString(pubData)
	arg.Set("pub", pubBase64)
	arg.Set("pk", pkBase64)
}

func decryptAES(arg js.Value) {
	password := arg.Get("password").String()
	ciphertext, _ := base64.StdEncoding.DecodeString(arg.Get("data").String())
	salt, _ := base64.StdEncoding.DecodeString(arg.Get("salt").String())
	nonce, _ := base64.StdEncoding.DecodeString(arg.Get("nonce").String())
	sha := sha256.New()
	sha.Write(salt)
	aesKey := sha.Sum([]byte(password))[:16]

	block, _ := aes.NewCipher(aesKey)
	aesGCM, _ := cipher.NewGCM(block)
	plain, _ := aesGCM.Open(nil, nonce, ciphertext, nil)
	arg.Set("plain", string(plain))
}

func encryptAES(arg js.Value) {
	secretMessage := arg.Get("plain").String()
	password := arg.Get("password").String()
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

	arg.Set("data", base64.StdEncoding.EncodeToString(ciphertext))
	arg.Set("nonce", base64.StdEncoding.EncodeToString(nonce))
	arg.Set("salt", base64.StdEncoding.EncodeToString(salt))
}

func encryptData(secretMessage []byte, rsaKey *rsa.PrivateKey) (string, string, string) {
	aesKey := make([]byte, 16)
	nonce := make([]byte, 12)
	rand.Read(aesKey)
	rand.Read(nonce)
	block, _ := aes.NewCipher(aesKey)
	aesGCM, _ := cipher.NewGCM(block)
	ciphertext := aesGCM.Seal(nil, nonce, secretMessage, nil)

	ciphertextKey, _ := rsa.EncryptOAEP(sha256.New(), rand.Reader, &rsaKey.PublicKey, aesKey, []byte{})

	return base64.StdEncoding.EncodeToString(ciphertext),
		base64.StdEncoding.EncodeToString(ciphertextKey),
		base64.StdEncoding.EncodeToString(nonce)
}

func decryptRSA(input js.Value, rsaKey *rsa.PrivateKey) []byte {
	ciphertext, _ := base64.StdEncoding.DecodeString(input.Get("data").String())
	ciphertextKey, _ := base64.StdEncoding.DecodeString(input.Get("key").String())
	nonce, _ := base64.StdEncoding.DecodeString(input.Get("nonce").String())
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

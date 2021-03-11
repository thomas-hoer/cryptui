package main

import (
	"crypto"
	"crypto/rsa"
	"crypto/sha256"
	"crypto/x509"
	"encoding/base64"
	"encoding/json"
	"io"
	"log"
	"math/rand"
	"net/http"
	"os"
	"path"
	"path/filepath"
	"strconv"
	"strings"
	"time"
)

const logRequests bool = true
const contentType = "Content-Type"

func main() {
	sh := &storageHandler{
		static:   "data/static",
		business: "data/business",
		user:     "data/user",
	}
	sh.init()
	http.Handle("/", handleMiddleware(gzipper(sh)))
	if len(os.Args) == 3 {
		certFile := os.Args[1]
		keyFile := os.Args[2]
		log.Fatal(http.ListenAndServeTLS(":443", certFile, keyFile, nil))
	} else {
		log.Fatal(http.ListenAndServe(":80", nil))
	}
}

func handleMiddleware(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		start := time.Now()
		var caching bool = true
		if strings.HasSuffix(r.RequestURI, ".css") {
			w.Header().Add(contentType, "text/css")
		} else if strings.HasSuffix(r.RequestURI, ".html") {
			w.Header().Add(contentType, "text/html")
		} else if strings.HasSuffix(r.RequestURI, ".ico") {
			w.Header().Add(contentType, "image/x-icon")
		} else if strings.HasSuffix(r.RequestURI, ".png") {
			w.Header().Add(contentType, "image/png")
		} else if strings.HasSuffix(r.RequestURI, ".jpg") || strings.HasSuffix(r.RequestURI, ".jpeg") {
			w.Header().Add(contentType, "image/jpeg")
		} else if strings.HasSuffix(r.RequestURI, ".wasm") {
			w.Header().Add(contentType, "application/wasm")
		} else if strings.HasSuffix(r.RequestURI, ".js") {
			w.Header().Add(contentType, "application/javascript")
		} else if strings.HasSuffix(r.RequestURI, "json") { // .json or ?json
			caching = strings.HasPrefix(r.RequestURI, "/files/")
			w.Header().Add(contentType, "application/json")
		} else if !strings.HasSuffix(r.RequestURI, "/") {
			caching = false
		} else {
			caching = false
		}
		if caching {
			w.Header().Add("Cache-Control", "public, max-age=2592000") //30 days
		}
		next.ServeHTTP(w, r)
		if logRequests {
			elapsed := time.Since(start)
			log.Printf("%v %v took %v", r.Method, r.RequestURI, elapsed)
		}
	})
}

type storageHandler struct {
	static   string
	business string
	user     string
}

func (handler *storageHandler) init() {
	os.MkdirAll(handler.static, os.ModePerm)
	os.MkdirAll(handler.business, os.ModePerm)
	os.MkdirAll(handler.user, os.ModePerm)
}

func (handler *storageHandler) ServeHTTP(resp http.ResponseWriter, req *http.Request) {
	splits := strings.Split(req.RequestURI, "?")
	requestURI := splits[0]
	var queryParam string = ""
	if len(splits) > 1 {
		queryParam = splits[1]
	}
	if req.Method == "GET" {
		handler.handleGetUser(resp, requestURI, queryParam)
	} else if req.Method == "POST" {
		handler.handlePostUser(resp, req)
	} else if req.Method == "PUT" {
		handler.handlePutUser(resp, req)
	} else {
		resp.Header().Set("Allow", "GET, POST")
		resp.WriteHeader(405)
	}
}

func (handler *storageHandler) handleGetUser(resp http.ResponseWriter, requestURI, queryParam string) {
	filename := handler.user + requestURI
	fileInfo, err := os.Stat(filename)
	if os.IsNotExist(err) {
		handler.handleGetType(resp, requestURI, queryParam)
	} else if fileInfo.IsDir() {
		if !strings.HasSuffix(filename, "/") {
			resp.Header().Set("Location", requestURI+"/")
			resp.WriteHeader(301)
		} else {
			handler.handleGetIndex(resp, handler.user, requestURI, queryParam)
		}
	} else {
		dat, _ := os.ReadFile(filename)
		resp.Write(dat)
	}
}

func (handler *storageHandler) handleGetIndex(resp http.ResponseWriter, base, requestURI, queryParam string) {
	fileInfos, _ := os.ReadDir(base + requestURI)
	names := make([]string, 0)
	for _, fileInfo := range fileInfos {
		if fileInfo.IsDir() {
			names = append(names, `"`+fileInfo.Name()+`/"`)
		} else {
			names = append(names, `"`+fileInfo.Name()+`"`)
		}
	}
	jsonOutput := `[` + strings.Join(names, `,`) + `]`
	if queryParam == "json" {
		resp.Header().Add(contentType, "application/json")
		resp.Write([]byte(jsonOutput))
	} else {
		resp.Header().Add(contentType, "text/html")
		data, _ := os.ReadFile(handler.static + "/index.html")
		resp.Write(data)
	}
}

func (handler *storageHandler) handleGetType(resp http.ResponseWriter, requestURI, queryParam string) {
	typefile := filepath.Dir(handler.user+requestURI) + "/type"
	if fileInfo, err := os.Stat(typefile); err == nil && !fileInfo.IsDir() {
		dat, _ := os.ReadFile(typefile)
		typeRoot := string(dat)
		filename := filepath.Base(handler.user + requestURI)
		redirect := "/" + typeRoot + "/" + filename
		if redirect == requestURI {
			handler.handleGetStatic(resp, requestURI, queryParam)
		} else {
			resp.Header().Add("Location", redirect)
			resp.WriteHeader(303)
		}
	} else {
		handler.handleGetStatic(resp, requestURI, queryParam)
	}
}
func (handler *storageHandler) handleGetStatic(resp http.ResponseWriter, requestURI, queryParam string) {
	if fileInfo, err := os.Stat(handler.static + requestURI); err == nil && !fileInfo.IsDir() {
		dat, _ := os.ReadFile(handler.static + requestURI)
		resp.Write(dat)
	} else {
		handler.handleGetBusiness(resp, requestURI, queryParam)
	}
}
func (handler *storageHandler) handleGetBusiness(resp http.ResponseWriter, requestURI, queryParam string) {
	if fileInfo, err := os.Stat(handler.business + requestURI); err == nil {
		if !fileInfo.IsDir() {
			dat, _ := os.ReadFile(handler.business + requestURI)
			resp.Write(dat)
		} else {
			handler.handleGetIndex(resp, handler.business, requestURI, queryParam)
		}
		return
	}
	resp.WriteHeader(404)
}

func (handler *storageHandler) handlePostUser(resp http.ResponseWriter, req *http.Request) {
	filename := handler.user + req.RequestURI
	fileInfo, err := os.Stat(filename)
	if os.IsNotExist(err) {
		resp.WriteHeader(404)
		return
	} else if !fileInfo.IsDir() {
		resp.Header().Set("Allow", "GET")
		resp.WriteHeader(405)
		return
	}

	contentType := typeOf(req)
	newData, _ := io.ReadAll(req.Body)
	newDataString := string(newData)

	newID := strconv.Itoa(rand.Int())
	newPath := filename + newID + "/"

	os.MkdirAll(newPath, os.ModePerm)
	os.WriteFile(newPath+"type", []byte(contentType), os.ModePerm)
	os.WriteFile(newPath+"data.json", []byte(newDataString), os.ModePerm)
	if len(req.Header["User-Id"]) != 0 {
		userID := req.Header["User-Id"][0]
		os.WriteFile(newPath+"user", []byte(userID), os.ModePerm)
	}
	resp.Header().Add("Id", newID)
	resp.Header().Add("Location", req.RequestURI+newID+"/")
	resp.WriteHeader(201)
}

type userData struct {
	Name string `json:"name"`
	Key  string `json:"key"`
}

func getUserData(userSpace, filename string) *userData {
	var dir string = filename
	for dir != "." {
		dir = path.Dir(dir)
		if typeDat, err := os.ReadFile(dir + string(os.PathSeparator) + "type"); err == nil {
			typeName := string(typeDat)
			if typeName == "user/instance" {
				var userData userData
				dat, _ := os.ReadFile(dir + string(os.PathSeparator) + "data.json")
				json.Unmarshal(dat, &userData)
				return &userData
			}
		}
		if userID, err := os.ReadFile(dir + string(os.PathSeparator) + "user"); err == nil {
			return getUserData(userSpace, userSpace+"/user/"+string(userID)+"/")
		}
	}
	return nil
}

func (handler *storageHandler) handlePutUser(resp http.ResponseWriter, req *http.Request) {
	filename := handler.user + req.RequestURI
	dir := path.Dir(filename)
	os.MkdirAll(dir, os.ModePerm)
	userData := getUserData(handler.user, filename)
	newData, _ := io.ReadAll(req.Body)
	if userData != nil {
		keyData, _ := base64.StdEncoding.DecodeString(userData.Key)
		pubKey, _ := x509.ParsePKCS1PublicKey(keyData)

		hashed := sha256.Sum256(newData)
		if len(req.Header["Signature"]) != 0 {
			signature, _ := base64.StdEncoding.DecodeString(req.Header["Signature"][0])
			err := rsa.VerifyPKCS1v15(pubKey, crypto.SHA256, hashed[:], signature)
			if err == nil {
				os.WriteFile(filename, newData, os.ModePerm)
				resp.WriteHeader(204)
			} else {
				resp.WriteHeader(403)
			}
		}
	}
	resp.WriteHeader(403)
}

func typeOf(req *http.Request) string {
	if req == nil {
		return ""
	}
	ct := strings.Split(req.Header.Get(contentType), "/")
	if len(ct) < 2 {
		return ""
	}
	return strings.Replace(ct[1], ".", "/", -1)
}

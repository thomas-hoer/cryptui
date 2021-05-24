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

type Handler interface {
	http.Handler
	getStatic() string
}

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
	static      string
	business    string
	user        string
	index       []byte
	idGenerator func(string) string
}

func (handler *storageHandler) getStatic() string {
	return handler.static
}
func (handler *storageHandler) init() {
	if index, err := os.ReadFile(handler.static + "/index.html"); err == nil {
		handler.index = index
	} else {
		log.Panic(err)
	}
	os.MkdirAll(handler.business, os.ModePerm)
	os.MkdirAll(handler.user, os.ModePerm)
	rand.Seed(time.Now().UnixNano())
	if handler.idGenerator == nil {
		handler.idGenerator = func(contentType string) string {
			return strconv.FormatInt(rand.Int63(), 36) + "-" + strconv.FormatInt(rand.Int63(), 36)
		}
	}
}

func (handler *storageHandler) ServeHTTP(resp http.ResponseWriter, req *http.Request) {
	if req.Method == "GET" {
		handler.handleGetUser(resp, req)
	} else if req.Method == "POST" {
		handler.handlePostUser(resp, req)
	} else if req.Method == "PUT" {
		handler.handlePutUser(resp, req)
	} else {
		resp.Header().Set("Allow", "GET, POST")
		resp.WriteHeader(http.StatusMethodNotAllowed)
	}
}

func (handler *storageHandler) handleGetUser(resp http.ResponseWriter, req *http.Request) {
	requestURI := req.URL.Path
	filename := handler.user + requestURI
	fileInfo, err := os.Stat(filename)
	if os.IsNotExist(err) {
		handler.handleGetType(resp, req)
	} else if fileInfo.IsDir() {
		if !strings.HasSuffix(filename, "/") {
			resp.Header().Set("Location", requestURI+"/")
			resp.WriteHeader(http.StatusMovedPermanently)
		} else {
			handler.handleGetIndex(resp, handler.user, req)
		}
	} else {
		etag := `\W"` + strconv.FormatInt(fileInfo.ModTime().UnixNano(), 36) + `"`
		if req.Header.Get("If-None-Match") == etag {
			resp.WriteHeader(http.StatusNotModified)
			return
		}
		resp.Header().Add("ETag", etag)
		if dat, err := os.Open(filename); err == nil {
			defer dat.Close()
			io.Copy(resp, dat)
		} else {
			log.Print(err.Error())
			resp.WriteHeader(http.StatusNotFound)
		}
	}
}

func (handler *storageHandler) handleGetIndex(resp http.ResponseWriter, base string, req *http.Request) {
	fileInfos, err := os.ReadDir(base + req.URL.Path)
	if err != nil {
		log.Print(err.Error())
		resp.WriteHeader(http.StatusNotFound)
		return
	}
	names := make([]string, 0)
	for _, fileInfo := range fileInfos {
		if fileInfo.IsDir() {
			names = append(names, `"`+fileInfo.Name()+`/"`)
		} else {
			names = append(names, `"`+fileInfo.Name()+`"`)
		}
	}
	jsonOutput := `[` + strings.Join(names, `,`) + `]`
	if req.URL.RawQuery == "json" {
		resp.Header().Add(contentType, "application/json")
		resp.Write([]byte(jsonOutput))
	} else {
		resp.Header().Add(contentType, "text/html")
		resp.Write(handler.index)
	}
}

func (handler *storageHandler) handleGetType(resp http.ResponseWriter, req *http.Request) {
	fileName := handler.user + req.URL.Path
	typefile := filepath.Dir(fileName) + "/type"
	if fileInfo, err := os.Stat(typefile); err == nil && !fileInfo.IsDir() {
		if dat, err := os.ReadFile(typefile); err == nil {
			typeRoot := string(dat)
			filename := filepath.Base(fileName)
			redirect := "/" + typeRoot + "/" + filename
			if redirect == req.URL.Path {
				handler.handleGetStatic(resp, req)
			} else {
				resp.Header().Add("Location", redirect)
				resp.WriteHeader(http.StatusSeeOther)
			}
			return
		}
	}

	handler.handleGetStatic(resp, req)
}
func (handler *storageHandler) handleGetStatic(resp http.ResponseWriter, req *http.Request) {
	fileName := handler.static + req.URL.Path
	if fileInfo, err := os.Stat(fileName); err == nil && !fileInfo.IsDir() {
		if dat, err := os.Open(fileName); err == nil {
			defer dat.Close()
			io.Copy(resp, dat)
			return
		}
	}

	handler.handleGetBusiness(resp, req)
}

func (handler *storageHandler) handleGetBusiness(resp http.ResponseWriter, req *http.Request) {
	requestURI := req.URL.Path
	if fileInfo, err := os.Stat(handler.business + requestURI); err == nil {
		if fileInfo.IsDir() {
			handler.handleGetIndex(resp, handler.business, req)
			return
		} else if dat, err := os.Open(handler.business + requestURI); err == nil {
			defer dat.Close()
			io.Copy(resp, dat)
			return
		}
	}
	resp.WriteHeader(http.StatusNotFound)
}

func (handler *storageHandler) handlePostUser(resp http.ResponseWriter, req *http.Request) {
	filename := handler.user + req.RequestURI
	fileInfo, err := os.Stat(filename)
	if os.IsNotExist(err) {
		resp.WriteHeader(http.StatusNotFound)
		return
	} else if !fileInfo.IsDir() || req.RequestURI == "/" {
		resp.Header().Set("Allow", "GET")
		resp.WriteHeader(http.StatusMethodNotAllowed)
		return
	}

	contentType := typeOf(req)
	if contentType == "" {
		resp.WriteHeader(http.StatusUnsupportedMediaType)
		return
	}
	newData, err := io.ReadAll(req.Body)
	if err != nil {
		resp.WriteHeader(http.StatusBadRequest)
		return
	}
	if contentType == "user/instance" {
		var userData userData
		if err := json.Unmarshal(newData, &userData); err == nil {
			if userData.Key == "" || userData.Name == "" {
				resp.WriteHeader(http.StatusBadRequest)
				return
			}
		} else {
			resp.WriteHeader(http.StatusBadRequest)
			return
		}
	}

	newDataString := string(newData)

	newID := handler.idGenerator(contentType)
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
	resp.WriteHeader(http.StatusCreated)
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
				if dat, err := os.ReadFile(dir + string(os.PathSeparator) + "data.json"); err == nil {
					json.Unmarshal(dat, &userData)
					return &userData
				}
				return nil
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
	if userData != nil {
		fileInfo, err := os.Stat(filename)
		ifMatch := req.Header.Get("If-Match")
		if err == nil {
			etag := `\W"` + strconv.FormatInt(fileInfo.ModTime().UnixNano(), 36) + `"`
			if ifMatch != etag {
				resp.WriteHeader(http.StatusPreconditionFailed)
				return
			}
		} else if ifMatch != "" && err != nil {
			resp.WriteHeader(http.StatusPreconditionFailed)
			return
		}

		keyData, err := base64.StdEncoding.DecodeString(userData.Key)
		if err != nil {
			resp.WriteHeader(500)
			return
		}
		pubKey, err := x509.ParsePKCS1PublicKey(keyData)
		if err != nil {
			resp.WriteHeader(500)
			return
		}

		newData, err := io.ReadAll(req.Body)
		if err != nil {
			resp.WriteHeader(400)
			return
		}
		hashed := sha256.Sum256(newData)
		if len(req.Header["Signature"]) != 0 {
			if signature, err := base64.StdEncoding.DecodeString(req.Header["Signature"][0]); err == nil {
				if err := rsa.VerifyPKCS1v15(pubKey, crypto.SHA256, hashed[:], signature); err == nil {
					os.WriteFile(filename, newData, os.ModePerm)
					fileInfo, _ := os.Stat(filename)
					etag := `\W"` + strconv.FormatInt(fileInfo.ModTime().UnixNano(), 36) + `"`
					resp.Header().Add("ETag", etag)
					resp.WriteHeader(http.StatusAccepted)
					return
				}
			}
		}
	}
	resp.WriteHeader(http.StatusForbidden)
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

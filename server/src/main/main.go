package main

import (
	"encoding/json"
	"io/ioutil"
	"log"
	mathrand "math/rand"
	"net/http"
	"os"
	"path/filepath"
	"strconv"
	"strings"
	"time"
)

const logRequests bool = true
const port string = ":8080"

func main() {

	sh := &StorageHandler{
		static:   "data/static",
		business: "data/business",
		user:     "data/user",
	}
	http.Handle("/", handleMiddleware(Gzip(sh)))
	log.Fatal(http.ListenAndServe(port, nil))
}

func handleMiddleware(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		start := time.Now()
		var caching bool = true
		if strings.HasSuffix(r.RequestURI, ".css") {
			w.Header().Add("Content-Type", "text/css")
		} else if strings.HasSuffix(r.RequestURI, ".html") {
			w.Header().Add("Content-Type", "text/html")
		} else if strings.HasSuffix(r.RequestURI, "/static/") {
			w.Header().Add("Content-Type", "text/html")
		} else if strings.HasSuffix(r.RequestURI, ".ico") {
			w.Header().Add("Content-Type", "image/x-icon")
		} else if strings.HasSuffix(r.RequestURI, ".png") {
			w.Header().Add("Content-Type", "image/png")
		} else if strings.HasSuffix(r.RequestURI, ".jpg") {
			w.Header().Add("Content-Type", "image/jpeg")
		} else if strings.HasSuffix(r.RequestURI, ".wasm") {
			w.Header().Add("Content-Type", "application/wasm")
		} else if strings.HasSuffix(r.RequestURI, ".js") {
			w.Header().Add("Content-Type", "application/javascript")
		} else if strings.HasSuffix(r.RequestURI, "json") { // .json or ?json
			caching = false
			w.Header().Add("Content-Type", "application/json")
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

type BusinessInfo struct {
	Name           string   `json:"name"`
	Instanceable   bool     `json:"instanceable"`
	Allow          []string `json:"allow"` // Allow other Business Types as Child
	CurrentVersion string   `json:"currentVersion"`

	//Version Specific
	//GetScript      string   `json:"getScript"`
	//PostScript     string   `json:"postScript"`
	// Component
	// Page
}
type StorageHandler struct {
	static   string
	business string
	user     string
}

func (handler *StorageHandler) ServeHTTP(resp http.ResponseWriter, req *http.Request) {
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
	} else if req.Method == "PATCH" {
		handler.handlePatchUser(resp, req)
	} else {
		resp.Header().Set("Allow", "GET, POST")
		resp.WriteHeader(405)
	}
}

func (handler *StorageHandler) handleGetUser(resp http.ResponseWriter, requestURI, queryParam string) {
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
		dat, _ := ioutil.ReadFile(filename)
		resp.Write(dat)
	}
}
func (handler *StorageHandler) handleGetIndex(resp http.ResponseWriter, base, requestURI, queryParam string) {
	fileInfos, _ := ioutil.ReadDir(base + requestURI)
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
		resp.Header().Add("Content-Type", "application/json")
		resp.Write([]byte(jsonOutput))
	} else if queryParam == "module" {
		resp.Header().Add("Content-Type", "application/javascript")
		resp.Write([]byte("'use strict';\nconst data=" + jsonOutput + "\nexport {data}"))
	} else {
		resp.Header().Add("Content-Type", "text/html")
		data, _ := ioutil.ReadFile(handler.static + "/index.html")
		resp.Write(data)
	}
}
func contains(list []string, stringToFind string) bool {
	for _, le := range list {
		if strings.Contains(le, stringToFind) {
			return true
		}
	}
	return false
}
func (handler *StorageHandler) handleGetType(resp http.ResponseWriter, requestURI, queryParam string) {
	typefile := filepath.Dir(handler.user+requestURI) + "/type"
	if fileInfo, err := os.Stat(typefile); err == nil && !fileInfo.IsDir() {
		dat, _ := ioutil.ReadFile(typefile)
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
func (handler *StorageHandler) handleGetStatic(resp http.ResponseWriter, requestURI, queryParam string) {
	if fileInfo, err := os.Stat(handler.static + requestURI); err == nil && !fileInfo.IsDir() {
		dat, _ := ioutil.ReadFile(handler.static + requestURI)
		resp.Write(dat)
	} else {
		handler.handleGetBusiness(resp, requestURI, queryParam)
	}
}
func (handler *StorageHandler) handleGetBusiness(resp http.ResponseWriter, requestURI, queryParam string) {
	if fileInfo, err := os.Stat(handler.business + requestURI); err == nil {
		if !fileInfo.IsDir() {
			dat, _ := ioutil.ReadFile(handler.business + requestURI)
			resp.Write(dat)
		} else {
			handler.handleGetIndex(resp, handler.business, requestURI, queryParam)
		}
		return
	}

	root := filepath.Dir(handler.business + requestURI)
	businessInfo := root + "/info.json"
	if fileInfo, err := os.Stat(businessInfo); err == nil && !fileInfo.IsDir() {
		dat, _ := ioutil.ReadFile(businessInfo)
		var bi BusinessInfo
		json.Unmarshal(dat, &bi)
		if bi.CurrentVersion != "" {
			filename := filepath.Base(handler.user + requestURI)
			redirect := "versions/" + bi.CurrentVersion + "/" + filename
			resp.Header().Add("Location", redirect)
			resp.WriteHeader(303)
			return
		}
	}
	resp.WriteHeader(404)

}

type Sequence struct {
	NextId int `json:"nextId"`
}

func (handler *StorageHandler) handlePostUser(resp http.ResponseWriter, req *http.Request) {
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

	bc := handler.getBusinessContext(req.RequestURI)
	dataType := typeOf(req)
	if dataType == nil {
		resp.WriteHeader(415)
		return
	}
	bc.setContentType(*dataType)
	newData, _ := ioutil.ReadAll(req.Body)
	newDataString := string(newData)

	newId := strconv.Itoa(mathrand.Int())
	newPath := filename + newId + "/"
	bc.setTargetURI(req.RequestURI + newId + "/")

	os.MkdirAll(newPath, os.ModePerm)
	ioutil.WriteFile(newPath+"type", []byte(*dataType), os.ModePerm)
	ioutil.WriteFile(newPath+"data.json", []byte(newDataString), os.ModePerm)
	resp.Header().Add("Id", newId)
	resp.Header().Add("Location", req.RequestURI+newId+"/"+bc.relocate)
	resp.WriteHeader(201)
}

func (handler *StorageHandler) handlePutUser(resp http.ResponseWriter, req *http.Request) {
	filename := handler.user + req.RequestURI

	bc := handler.getBusinessContext(req.RequestURI)

	if contentTypeData, err := ioutil.ReadFile(filename + "type"); err != nil {
		bc.setContentType(string(contentTypeData))
	}
	newData, _ := ioutil.ReadAll(req.Body)
	newDataString := string(newData)
	ioutil.WriteFile(filename, []byte(newDataString), os.ModePerm)
	resp.WriteHeader(204)
}

func (handler *StorageHandler) handlePatchUser(resp http.ResponseWriter, req *http.Request) {
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

	bc := handler.getBusinessContext(req.RequestURI)

	if contentTypeData, err := ioutil.ReadFile(filename + "type"); err != nil {
		bc.setContentType(string(contentTypeData))
	}
	if dataType := typeOf(req); dataType != nil {
		ioutil.WriteFile(filename+"type", []byte(*dataType), os.ModePerm)
		bc.setContentType(*dataType)
	}
	dataString := readAsJsString(filename + "data.json")
	ioutil.WriteFile(filename+"data.json", []byte(dataString), os.ModePerm)
	resp.WriteHeader(201)
}

func readAsJsString(path string) string {
	if dat, err := ioutil.ReadFile(path); err == nil {
		return string(dat)
	} else {
		return "null"
	}

}
func typeOf(req *http.Request) *string {
	ct := strings.Split(req.Header.Get("Content-Type"), "/")
	if len(ct) < 2 {
		return nil
	}
	applicationType := strings.Replace(ct[1], ".", "/", -1)
	return &applicationType
}

type businessContext struct {
	requestURI   string
	targetURI    string
	newId        string
	contentType  string
	rootBusiness string
	rootUser     string
	relocate     string
}

func (handler *StorageHandler) getBusinessContext(requestURI string) *businessContext {
	bc := &businessContext{
		requestURI:   requestURI,
		targetURI:    requestURI,
		rootBusiness: handler.business,
		rootUser:     handler.user,
	}
	return bc
}

func (bc *businessContext) setContentType(contentType string) {
	bc.contentType = contentType
}
func (bc *businessContext) setTargetURI(targetURI string) {
	bc.targetURI = targetURI
}

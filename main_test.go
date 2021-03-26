package main

import (
	"bytes"
	"crypto"
	"crypto/rand"
	"crypto/rsa"
	"crypto/sha256"
	"crypto/x509"
	"encoding/base64"
	"io"
	"log"
	"net/http"
	"os"
	"strconv"
	"strings"
	"testing"
)

const pathSeparator = string(os.PathSeparator)

var typeOfTests = []struct {
	expected string
	request  *http.Request
}{
	{"", nil},
	{"", &http.Request{
		Header: make(map[string][]string),
	}},
	{"", &http.Request{
		Header: map[string][]string{
			"Content-Type": {""},
		},
	}},
	{"a/b", &http.Request{
		Header: map[string][]string{
			"Content-Type": {"application/a.b"},
		},
	}},
	{"user", &http.Request{
		Header: map[string][]string{
			"Content-Type": {"application/user"},
		},
	}},
	{"plain", &http.Request{
		Header: map[string][]string{
			"Content-Type": {"text/plain"},
		},
	}},
}

func TestTypeOf(t *testing.T) {
	for i, e := range typeOfTests {
		res := typeOf(e.request)
		if res != e.expected {
			t.Errorf("typeOf(%d): expected %s, actual %s", i, e.expected, res)
		}
	}
}

type mockHandler struct {
}

type mockResponseWriter struct {
	body       []byte
	statusCode int
	header     http.Header
}

func (w *mockResponseWriter) Header() http.Header {
	return w.header
}

func (w *mockResponseWriter) Write(b []byte) (int, error) {
	w.body = append(w.body, b...)
	return 0, nil
}

func (w *mockResponseWriter) WriteHeader(statusCode int) {
	w.statusCode = statusCode
}

func (h *mockHandler) ServeHTTP(writer http.ResponseWriter, req *http.Request) {
}

var handleMiddlewareTests = []struct {
	request  string
	expected string
	cached   bool
}{
	{"/x.css", "text/css", true},
	{"/index.html", "text/html", true},
	{"/", "", false},
	{"/favicon.ico", "image/x-icon", true},
	{"//.jpg", "image/jpeg", true},
	{"//.jpeg", "image/jpeg", true},
	{"png.png", "image/png", true},
	{"main.wasm", "application/wasm", true},
	{"/static/preact.js", "application/javascript", true},
	{"?json", "application/json", false},
	{"//", "", false},
	{"abc.xyz", "", false},
}

func TestHandleMiddleware(t *testing.T) {
	handler := handleMiddleware(&mockHandler{})
	for i, e := range handleMiddlewareTests {
		writer := &mockResponseWriter{
			header: make(map[string][]string),
		}
		req := &http.Request{
			RequestURI: e.request,
		}
		buf := bytes.NewBufferString("")
		log.SetOutput(buf)
		handler.ServeHTTP(writer, req)
		expected := e.request + " took "
		if logRequests && !strings.Contains(buf.String(), expected) {
			t.Errorf("handleMiddleware()#%d logging: expected part %s, actual %s", i, expected, buf.String())
		}
		actual := writer.header.Get("Content-Type")
		if actual != e.expected {
			t.Errorf("handleMiddleware()#%d Content-Type: expected %s, actual %s", i, e.expected, actual)
		}
		if _, ok := writer.header["Cache-Control"]; ok != e.cached {
			t.Errorf("handleMiddleware()#%d Cache-Control: expected %t, actual %t", i, e.cached, ok)
		}
	}

}

func TestStorageHandlerInitPanic(t *testing.T) {
	dir, err := os.MkdirTemp("", "test")
	if err != nil {
		t.Fatal(err)
	}
	defer os.RemoveAll(dir) // clean up

	sh := &storageHandler{
		static: dir,
	}
	defer func() {
		if r := recover(); r == nil {
			t.Errorf("storageHandler.init(): expected panic, actual no panic")
		}
	}()
	sh.init()
}

const index = "<html></html>"

func TestStorageHandlerInit(t *testing.T) {
	dir, err := os.MkdirTemp("", "test")
	if err != nil {
		t.Fatal(err)
	}
	defer os.RemoveAll(dir) // clean up

	sh := &storageHandler{
		static:   dir + pathSeparator + "static",
		business: dir + pathSeparator + "business",
		user:     dir + pathSeparator + "user",
	}
	if err := os.MkdirAll(sh.static, os.ModePerm); err != nil {
		t.Fatal(err)
	}
	if err := os.WriteFile(sh.static+pathSeparator+"index.html", []byte(index), os.ModePerm); err != nil {
		t.Fatal(err)
	}
	sh.init()
	if fileInfo, err := os.Stat(sh.business); os.IsNotExist(err) {
		t.Errorf("storageHandler.init(): expected %s exists, actual not exists", sh.business)
	} else if !fileInfo.IsDir() {
		t.Errorf("storageHandler.init(): expected %s is dir, actual is not dir", sh.business)
	}
	if fileInfo, err := os.Stat(sh.user); os.IsNotExist(err) {
		t.Errorf("storageHandler.init(): expected %s exists, actual not exists", sh.user)
	} else if !fileInfo.IsDir() {
		t.Errorf("storageHandler.init(): expected %s is dir, actual is not dir", sh.user)
	}
	if string(sh.index) != index {
		t.Errorf("storageHandler.init() index: expected %s, actual %s", index, sh.index)
	}
	if id := sh.idGenerator(""); id == "" {
		t.Errorf("storageHandler.init() idGenerator: expected not empty, actual empty")
	}
}

func readFile(filename string) []byte {
	dat, _ := os.ReadFile(filename)
	return dat
}

var serveHTTPGetTests = []struct {
	request     string
	statusCode  int
	contentType string
	location    string
	body        []byte
}{
	{"", 301, "", "/", []byte("")},
	{"/", 0, "text/html", "", readFile("data/static/index.html")},
	{"/js/preact.js", 0, "", "", readFile("data/static/js/preact.js")},
	{"/?json", 0, "application/json", "", []byte(`["files/","profile/","user/"]`)},
	{"/user/1/page.js", 303, "", "/user/instance/page.js", []byte("")},
	{"/user/1/type", 0, "", "", []byte("user/instance")},
	{"/user/files", 303, "", "/users/files", []byte("")},
	{"/user/?json", 0, "application/json", "", []byte(`["1/","type"]`)},
	{"/users/", 404, "", "", []byte("")},
	{"/users/page.js", 404, "", "", []byte("")},
	{"/profile/page.js", 0, "", "", readFile("data/business/profile/page.js")},
	{"/profile/", 0, "text/html", "", readFile("data/static/index.html")},
	{"/folder/instance/", 0, "text/html", "", readFile("data/static/index.html")},
}

// The following privateKey and publicKey constants represent a 512 bit rsa key
// pair. It should not be used outside this test. First of all, it is public
// visible on github and maybe others. Second, it is a 512 bit key, which is
// considered as not secure, due to its size. Using a small key makes the test
// faster.
const privateKey = "MIIBOgIBAAJBAOOK4kUijqVb9zm7riuF126Zm+111AF3YpepnF6CUTdp7HY9jVdvmYZaw8lsdk3JjmvK7EiTr+I0pzeBivuZBacCAwEAAQJBAJaEUJka+vE3nJp8JAJ2TsPCqPqzbsJpjrZ0ZBPAcKkOESNI3XGwXn5m+M1FBLQ7HQIw8QIAbZScR67HUL/GIoECIQDzb0CjtHGECZLLhstybf79ww9EDf8pyWRWZGT1drrRSQIhAO9Joihzz70/hjb4tZnDWmg8kBT17iSPtXUWe8cgrQ9vAiBpHZIQ3lriA+xCPCtfdwXTd8YAwfZ7ib3s3B8IK0OSGQIgVxaJegeMV+hCxMcH8Qp0YPOJzNck8RGMjSy9p99wnOkCIBuNZ0yYrlf+TZ2tP/fujdoDqmRwAa8xEdk354dAJ7dM"
const publicKey = "MEgCQQDjiuJFIo6lW/c5u64rhddumZvtddQBd2KXqZxeglE3aex2PY1Xb5mGWsPJbHZNyY5ryuxIk6/iNKc3gYr7mQWnAgMBAAE="

var nextID int

func signData(data string) string {
	b64 := base64.StdEncoding
	pkBytes, _ := b64.DecodeString(privateKey)
	pk, _ := x509.ParsePKCS1PrivateKey(pkBytes)

	hashed := sha256.Sum256([]byte(data))
	signature, _ := rsa.SignPKCS1v15(rand.Reader, pk, crypto.SHA256, hashed[:])
	return b64.EncodeToString(signature)
}

var serveHTTPPostTests = []struct {
	method        string
	request       string
	requestBody   []byte
	requestHeader map[string][]string
	statusCode    int
	responseBody  []byte
	headerID      string
	location      string
}{
	{ //#0
		"POST",
		"/user/",
		[]byte(`{"name":"a","key":"` + publicKey + `"}`),
		map[string][]string{
			"Content-Type": {"application/user.instance"},
		},
		http.StatusCreated,
		[]byte(""),
		"1",
		"/user/1/",
	},
	{ //#1
		"PUT",
		"/user/1/files",
		[]byte("{}"),
		map[string][]string{},
		http.StatusForbidden,
		[]byte(""),
		"",
		"",
	},
	{ //#2
		"PUT",
		"/user/1/files",
		[]byte("{}"),
		map[string][]string{
			"Signature": {signData("{}")},
		},
		http.StatusAccepted,
		[]byte(""),
		"",
		"",
	},
	{ //#3
		"GET",
		"/user/1/files",
		[]byte{},
		map[string][]string{},
		0,
		[]byte("{}"),
		"",
		"",
	},
	{ //#4
		"PUT",
		"/user/1/files",
		[]byte("{...}"),
		map[string][]string{
			"Signature": {signData("{/}")},
		},
		http.StatusForbidden,
		[]byte(""),
		"",
		"",
	},
	{ //#5
		"GET",
		"/user/1/files",
		[]byte{},
		map[string][]string{},
		0,
		[]byte("{}"),
		"",
		"",
	},
	{ //#6
		"POST",
		"/user/",
		[]byte(`{}`),
		map[string][]string{
			"Content-Type": {"application/user.instance"},
		},
		http.StatusBadRequest,
		[]byte(""),
		"",
		"",
	},
	{ //#7
		"POST",
		"/user/",
		[]byte(`{"name":"a","key":"` + publicKey + `"}`),
		map[string][]string{
			"Content-Type": {""},
		},
		http.StatusUnsupportedMediaType,
		[]byte(""),
		"",
		"",
	},
	{ //#8
		"POST",
		"/user/",
		[]byte(`////`),
		map[string][]string{
			"Content-Type": {"application/user.instance"},
		},
		http.StatusBadRequest,
		[]byte(""),
		"",
		"",
	},
	{ //#9
		"POST",
		"/user/",
		nil,
		map[string][]string{
			"Content-Type": {"application/user.instance"},
		},
		http.StatusBadRequest,
		[]byte(""),
		"",
		"",
	},
	{ //#10
		"POST",
		"/use/",
		[]byte(`{"name":"a","key":"` + publicKey + `"}`),
		map[string][]string{
			"Content-Type": {"application/user.instance"},
		},
		http.StatusNotFound,
		[]byte(""),
		"",
		"",
	},
	{ //#11
		"POST",
		"/",
		[]byte(`{"name":"a","key":"` + publicKey + `"}`),
		map[string][]string{
			"Content-Type": {"application/user.instance"},
		},
		http.StatusMethodNotAllowed,
		[]byte(""),
		"",
		"",
	},
	{ //#12
		"POST",
		"/index.html",
		[]byte(`{"name":"a","key":"` + publicKey + `"}`),
		map[string][]string{
			"Content-Type": {"application/user.instance"},
		},
		http.StatusNotFound,
		[]byte(""),
		"",
		"",
	},
	{ //#13
		"POST",
		"/page.js",
		[]byte(`{"name":"a","key":"` + publicKey + `"}`),
		map[string][]string{
			"Content-Type": {"application/user.instance"},
		},
		http.StatusNotFound,
		[]byte(""),
		"",
		"",
	},
	{ //#14
		"PATCH",
		"/user/1/files",
		[]byte(`{}`),
		map[string][]string{},
		http.StatusMethodNotAllowed,
		[]byte(""),
		"",
		"",
	},
	{ //#15
		"POST",
		"/user/",
		[]byte(`{"name":"b","key":"AAAA"}`),
		map[string][]string{
			"Content-Type": {"application/user.instance"},
		},
		http.StatusCreated,
		[]byte(""),
		"2",
		"/user/2/",
	},
	{ //#16
		"PUT",
		"/user/2/files",
		[]byte("{}"),
		map[string][]string{
			"Signature": {signData("{}")},
		},
		http.StatusInternalServerError,
		[]byte(""),
		"",
		"",
	},
	{ //#17
		"POST",
		"/user/",
		[]byte(`{"name":"c","key":"!!!!"}`),
		map[string][]string{
			"Content-Type": {"application/user.instance"},
		},
		http.StatusCreated,
		[]byte(""),
		"3",
		"/user/3/",
	},
	{ //#18
		"PUT",
		"/user/3/files",
		[]byte("{}"),
		map[string][]string{
			"Signature": {signData("{}")},
		},
		http.StatusInternalServerError,
		[]byte(""),
		"",
		"",
	},
	{ //#19
		"GET",
		"/user/4/",
		[]byte("{}"),
		map[string][]string{},
		http.StatusNotFound,
		[]byte(""),
		"",
		"",
	},
	{ //#20
		"POST",
		"/user/",
		nil,
		map[string][]string{
			"Content-Type": {"application/user.instance"},
		},
		http.StatusBadRequest,
		[]byte(""),
		"",
		"",
	},
	{ //#21
		"POST",
		"/files/",
		[]byte(`{"name":"b","key":"AAAA"}`),
		map[string][]string{
			"Content-Type": {"application/file.instance"},
			"User-Id":      {"2"},
		},
		http.StatusCreated,
		[]byte(""),
		"4",
		"/files/4/",
	},
}

var serveHTTPTestsFileAsserts = []struct {
	path     string
	expected string
}{
	{"/files/4/user", "2"},
	{"/files/4/type", "file/instance"},
	{"/files/4/data.json", `{"name":"b","key":"AAAA"}`},
	{"/user/1/files", "{}"},
	{"/user/1/data.json", `{"name":"a","key":"` + publicKey + `"}`},
	{"/user/1/type", "user/instance"},
}

func TestStorageHandlerServeHTTP(t *testing.T) {
	dir, err := os.MkdirTemp("", "test")
	if err != nil {
		t.Fatal(err)
	}
	defer os.RemoveAll(dir) // clean up
	sh := &storageHandler{
		static:   "data/static",
		business: "data/business",
		user:     dir,
		idGenerator: func(contentType string) string {
			nextID++
			return strconv.Itoa(nextID)
		},
	}
	userDir := dir + pathSeparator + "user" + pathSeparator + "1"
	filesDir := dir + pathSeparator + "files"
	profileDir := dir + pathSeparator + "profile"
	os.MkdirAll(userDir, os.ModePerm)
	os.MkdirAll(filesDir, os.ModePerm)
	os.MkdirAll(profileDir, os.ModePerm)
	os.WriteFile(userDir+pathSeparator+"type", []byte("user/instance"), os.ModePerm)
	os.WriteFile(dir+pathSeparator+"user"+pathSeparator+"type", []byte("users"), os.ModePerm)
	os.WriteFile(profileDir+pathSeparator+"type", []byte("profile"), os.ModePerm)
	sh.init()
	for i, e := range serveHTTPGetTests {
		resp := &mockResponseWriter{
			header: make(map[string][]string),
		}
		req := &http.Request{
			Header:     make(map[string][]string),
			Method:     "GET",
			RequestURI: e.request,
		}
		sh.ServeHTTP(resp, req)
		if resp.statusCode != e.statusCode {
			t.Errorf("storageHandler.ServeHTTP()#%d resp.statusCode: expected: %d, actual: %d", i, e.statusCode, resp.statusCode)
		}
		contentType := resp.header.Get("Content-Type")
		if contentType != e.contentType {
			t.Errorf("storageHandler.ServeHTTP()#%d resp.header['Content-Type']: expected: %s, actual: %s", i, e.contentType, contentType)
		}
		location := resp.header.Get("Location")
		if location != e.location {
			t.Errorf("storageHandler.ServeHTTP()#%d resp.header['Location']: expected: %s, actual: %s", i, e.location, location)
		}
		if !bytes.Equal(resp.body, e.body) {
			t.Errorf("storageHandler.ServeHTTP()#%d resp.body: expected: %s, actual: %s", i, e.body, resp.body)
		}
	}

	for i, e := range serveHTTPPostTests {
		resp := &mockResponseWriter{
			header: make(map[string][]string),
		}
		req := &http.Request{
			Header:     e.requestHeader,
			Method:     e.method,
			RequestURI: e.request,
			Body:       io.NopCloser(bytes.NewReader(e.requestBody)),
		}

		sh.ServeHTTP(resp, req)
		if resp.statusCode != e.statusCode {
			t.Errorf("storageHandler.ServeHTTP()#%d resp.statusCode: expected: %d, actual: %d", i, e.statusCode, resp.statusCode)
		}
		if !bytes.Equal(resp.body, e.responseBody) {
			t.Errorf("storageHandler.ServeHTTP()#%d resp.body: expected: %s, actual: %s", i, e.responseBody, resp.body)
		}
		if headerID := resp.header.Get("Id"); headerID != e.headerID {
			t.Errorf("storageHandler.ServeHTTP()#%d resp.header['Id']: expected: %s, actual: %s", i, e.headerID, headerID)
		}
		if headerLocation := resp.header.Get("Location"); headerLocation != e.location {
			t.Errorf("storageHandler.ServeHTTP()#%d resp.header['Location']: expected: %s, actual: %s", i, e.location, headerLocation)
		}
	}
	for i, e := range serveHTTPTestsFileAsserts {
		dat, err := os.ReadFile(dir + e.path)
		if err != nil {
			t.Errorf("storageHandler.ServeHTTP()#%d readFile: expected: no error, actual: %s", i, err)
		} else {
			str := string(dat)
			if str != e.expected {
				t.Errorf("storageHandler.ServeHTTP()#%d readFile: expected: %s, actual: %s", i, e.expected, str)
			}
		}
	}
	err = os.RemoveAll(dir)
	if err != nil {
		t.Errorf("storageHandler.ServeHTTP() cleanup: expected: no error, actual: %s", err)
	}
}

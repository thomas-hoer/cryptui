package main

import (
	"bytes"
	"io"
	"log"
	"net/http"
	"os"
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
	{"/?json", 0, "application/json", "", []byte(`["profile/","user/"]`)},
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

var serveHTTPPostTests = []struct {
	method       string
	request      string
	requestBody  []byte
	statusCode   int
	responseBody []byte
	hasId        bool
}{
	{"POST", "/user/", []byte("{}"), 201, []byte(""), true},
	{"PUT", "/user/files", []byte("{}"), 403, []byte(""), false},
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
	}
	userDir := dir + pathSeparator + "user" + pathSeparator + "1"
	profileDir := dir + pathSeparator + "profile"
	os.MkdirAll(userDir, os.ModePerm)
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
			Header:     make(map[string][]string),
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
		if idHeader, ok := resp.header["Id"]; ok != e.hasId {
			t.Errorf("storageHandler.ServeHTTP()#%d resp.header['Id']: expected: %t, actual: %t (%s)", i, e.hasId, ok, idHeader)
		}
	}
}

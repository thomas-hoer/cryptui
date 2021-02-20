package main

import (
	"net/http"
	"testing"
)

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
			"Content-Type": []string{""},
		},
	}},
	{"a/b", &http.Request{
		Header: map[string][]string{
			"Content-Type": []string{"application/a.b"},
		},
	}},
	{"user", &http.Request{
		Header: map[string][]string{
			"Content-Type": []string{"application/user"},
		},
	}},
	{"plain", &http.Request{
		Header: map[string][]string{
			"Content-Type": []string{"text/plain"},
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
	w.body = b
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
}{
	{"/x.css", "text/css"},
	{"/index.html", "text/html"},
	{"/", ""},
	{"/favicon.ico", "image/x-icon"},
	{"//.jpg", "image/jpeg"},
	{"//.jpeg", "image/jpeg"},
	{"png.png", "image/png"},
	{"main.wasm", "application/wasm"},
	{"/static/preact.js", "application/javascript"},
	{"?json", "application/json"},
	{"//", ""},
}

func TestHandleMiddleware(t *testing.T) {
	handler := handleMiddleware(&mockHandler{})
	for _, e := range handleMiddlewareTests {
		writer := &mockResponseWriter{
			header: make(map[string][]string),
		}
		req := &http.Request{
			RequestURI: e.request,
		}
		handler.ServeHTTP(writer, req)
		actual := writer.header.Get("Content-Type")
		if actual != e.expected {
			t.Errorf("handleMiddleware(%s): expected %s, actual %s", e.request, e.expected, actual)
		}
	}
}

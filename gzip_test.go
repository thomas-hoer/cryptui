package main

import (
	"bytes"
	"compress/gzip"
	"io"
	"net/http"
	"os"
	"testing"
)

type handler struct {
	storageHandler
	statusCode int
	data       []byte
}

func (h *handler) ServeHTTP(resp http.ResponseWriter, req *http.Request) {
	resp.WriteHeader(h.statusCode)
	resp.Write(h.data)
}

var resp1 []byte = []byte{31, 139, 8, 0, 0, 0, 0, 0, 0, 255, 10, 201, 200, 44, 86, 200, 44, 86, 72, 84, 40, 74, 45, 46, 200, 207, 43, 78, 5, 4, 0, 0, 255, 255, 237, 82, 180, 232, 18, 0, 0, 0}
var resp2 []byte = []byte{31, 139, 8, 0, 0, 0, 0, 0, 0, 255, 36, 202, 177, 17, 195, 48, 12, 3, 192, 85, 144, 62, 227, 100, 1, 94, 204, 179, 228, 2, 212, 17, 80, 227, 233, 93, 184, 255, 223, 152, 194, 20, 130, 229, 145, 141, 78, 173, 162, 242, 139, 107, 203, 112, 33, 169, 221, 9, 143, 48, 206, 123, 46, 28, 149, 2, 203, 232, 244, 110, 190, 48, 240, 47, 202, 65, 127, 158, 0, 0, 0, 255, 255, 202, 27, 166, 232, 83, 0, 0, 0}
var gzipperTests = []struct {
	acceptEncoding []string
	data           []byte
	expected       []byte
}{
	{
		[]string{"gzip"},
		[]byte("This is a response"),
		resp1,
	},
	{
		[]string{"gzip"},
		[]byte("This is another response, just to ensure that gzip does not return just a constant!"),
		resp2,
	},
	{
		[]string{"gzip, deflate, br"},
		[]byte("This is a response"),
		resp1,
	},
	{
		[]string{"deflate, gzip, br"},
		[]byte("This is a response"),
		resp1,
	},
	{
		[]string{"deflate"},
		[]byte("This is a response"),
		[]byte("This is a response"),
	},
	{
		[]string{},
		[]byte("This is a response"),
		[]byte("This is a response"),
	},
}

func TestGzipper(t *testing.T) {
	dir, err := os.MkdirTemp("", "test")
	if err != nil {
		t.Fatal(err)
	}
	defer os.RemoveAll(dir) // clean up

	for i, e := range gzipperTests {
		h := gzipper(&handler{
			storageHandler: storageHandler{
				static: dir,
			},
			statusCode: http.StatusOK,
			data:       e.data,
		})
		resp := &mockResponseWriter{
			header: make(map[string][]string),
		}
		req := &http.Request{
			Header: map[string][]string{
				"Accept-Encoding": e.acceptEncoding,
			},
		}
		h.ServeHTTP(resp, req)
		expected := e.expected
		if !bytes.Equal(resp.body, expected) {
			t.Errorf("gzipper()#%d resp.body: expected: %ds, actual: %ds", i, expected, resp.body)
		}

		if resp.header.Get("Content-Encoding") == "gzip" {
			reader, err := gzip.NewReader(bytes.NewReader(resp.body))
			if err != nil {
				t.Errorf("gzipper()#%d gzip.NewReader: %s", i, err)
				continue
			}
			uncompress, err := io.ReadAll(reader)
			if err != nil {
				t.Errorf("gzipper()#%d io.ReadAll: %s", i, err)
				continue
			}
			if !bytes.Equal(uncompress, e.data) {
				t.Errorf("gzipper()#%d resp.data: expected: %s, actual: %s", i, e.data, uncompress)
			}
		}
	}
}

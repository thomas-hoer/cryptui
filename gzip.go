package main

import (
	"bytes"
	"compress/gzip"
	"io"
	"io/fs"
	"log"
	"net/http"
	"os"
	"path/filepath"
	"strings"
)

type gzipResponseWriter struct {
	io.Writer
	http.ResponseWriter
}

func (w gzipResponseWriter) Write(b []byte) (int, error) {
	return w.Writer.Write(b)
}
func gzipper(handler Handler) http.Handler {
	cache := make(map[string][]byte)
	static := strings.ReplaceAll(handler.getStatic(), `\`, "/")
	if err := filepath.WalkDir(static, func(path string, info fs.DirEntry, err error) error {
		if info.IsDir() {
			return nil
		}
		key := strings.Replace(strings.ReplaceAll(path, `\`, "/"), static, "", 1)
		dat, err := os.ReadFile(path)
		if err != nil {
			return err
		}
		var buf bytes.Buffer
		writer := gzip.NewWriter(&buf)
		writer.Write(dat)
		writer.Close()
		cache[key] = buf.Bytes()
		return nil
	}); err != nil {
		log.Panic(err)
	}
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if !strings.Contains(r.Header.Get("Accept-Encoding"), "gzip") {
			handler.ServeHTTP(w, r)
			return
		}
		w.Header().Set("Content-Encoding", "gzip")
		if dat, ok := cache[r.RequestURI]; ok {
			w.Write(dat)
			return
		}
		gz := gzip.NewWriter(w)
		defer gz.Close()
		gzw := gzipResponseWriter{Writer: gz, ResponseWriter: w}
		handler.ServeHTTP(gzw, r)
	})
}

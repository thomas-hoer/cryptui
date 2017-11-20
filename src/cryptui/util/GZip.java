/*
 * Copyright 2017 thomas-hoer.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cryptui.util;

import cryptui.DataType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

public class GZip {

    public static byte[] compress(byte[] input) {
        ByteArrayOutputStream obj = new ByteArrayOutputStream();
        obj.write(DataType.GZIP.getNumber());
        try (GZIPOutputStream gzip = new GZIPOutputStream(obj)) {
            gzip.write(input);
            gzip.close();
            byte[] compressed = obj.toByteArray();
            System.out.println("Compressed from " + input.length + " to " + compressed.length + " ( " + (compressed.length * 100 / input.length) + "%)");
            if (compressed.length < input.length) {
                return compressed;
            }
        } catch (IOException ex) {
            Logger.getLogger(GZip.class.getName()).log(Level.SEVERE, null, ex);
        }
        return input;
    }

}

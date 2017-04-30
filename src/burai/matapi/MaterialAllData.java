/*
 * Copyright (C) 2016 Satomichi Nishihara
 *
 * This file is distributed under the terms of the
 * GNU General Public License. See the file `LICENSE'
 * in the root directory of the present distribution,
 * or http://www.gnu.org/copyleft/gpl.txt .
 */

package burai.matapi;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import com.google.gson.Gson;

public class MaterialAllData extends MaterialData {

    private static final String MATERIALS_API_VASP = "/vasp/cif";

    public static MaterialAllData getInstance(String matID, String apiKey) {
        if (matID == null || matID.trim().isEmpty()) {
            throw new IllegalArgumentException("matID is empty.");
        }

        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("apiKey is empty.");
        }

        MaterialAllData matData = null;

        try {
            matData = readURL(matID.trim(), apiKey.trim());
        } catch (IOException e) {
            e.printStackTrace();
            matData = null;
        }

        return matData;
    }

    private static MaterialAllData readURL(String matID, String apiKey) throws IOException {
        Reader reader = null;
        MaterialAllData matData = null;

        try {
            URL url = new URL(MaterialsAPI.MATERIALS_API_URL + matID + MATERIALS_API_VASP);
            URLConnection urlConnection = url.openConnection();
            if (urlConnection == null) {
                throw new IOException("urlConnection is null.");
            }

            urlConnection.setRequestProperty("X-API-KEY", apiKey);

            InputStream input = urlConnection.getInputStream();
            input = input == null ? null : new BufferedInputStream(input);
            if (input == null) {
                throw new IOException("input is null.");
            }

            reader = new InputStreamReader(input);

            Gson gson = new Gson();
            matData = gson.<MaterialAllData> fromJson(reader, MaterialAllData.class);

        } catch (IOException e1) {
            throw e1;

        } catch (Exception e2) {
            throw new IOException(e2);

        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e3) {
                    throw e3;
                }
            }
        }

        return matData;
    }

    private List<MaterialUnit> response;

    private MaterialAllData() {
        this.response = null;
    }

    @Override
    public String getCIF() {
        if (this.response == null || this.response.isEmpty()) {
            return null;
        }

        MaterialUnit matUnit = this.response.get(0);
        return matUnit == null ? null : matUnit.getCif();
    }
}

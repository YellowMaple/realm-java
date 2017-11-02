/*
 * Copyright 2017 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.realm;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.JsonReader;
import android.util.JsonToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;
import java.util.Locale;

import javax.annotation.Nullable;

import io.realm.internal.OsList;
import io.realm.internal.android.JsonUtils;

class ProxyUtils {

    private static void throwWrongElementType(@Nullable  Class clazz) {
        throw new IllegalArgumentException(String.format(Locale.ENGLISH, "Element type '%s' is not handled.",
                clazz));
    }

    static <E> void setRealmListWithJsonObject(
            RealmList<E> realmList, JSONObject jsonObject, String fieldName) throws JSONException {
        if (!jsonObject.has(fieldName))  {
            return;
        }

        OsList osList = realmList.getOsList();
        if (jsonObject.isNull(fieldName)) {
            osList.removeAll();
        }

        JSONArray jsonArray = jsonObject.getJSONArray(fieldName);
        realmList.getOsList().removeAll();
        int arraySize = jsonArray.length();

        if (realmList.clazz == Boolean.class) {
            for (int i = 0; i < arraySize; i++) {
                if (jsonArray.isNull(i)) {
                    osList.addNull();
                } else {
                    osList.addBoolean(jsonArray.getBoolean(i));
                }
            }
        } else if (realmList.clazz == Float.class) {
            for (int i = 0; i < arraySize; i++) {
                if (jsonArray.isNull(i)) {
                    osList.addNull();
                } else {
                    osList.addFloat((float) jsonArray.getDouble(i));
                }
            }
        } else if (realmList.clazz == Double.class) {
            for (int i = 0; i < arraySize; i++) {
                if (jsonArray.isNull(i)) {
                    osList.addNull();
                } else {
                    osList.addDouble(jsonArray.getDouble(i));
                }
            }
        } else if (realmList.clazz == String.class) {
            for (int i = 0; i < arraySize; i++) {
                osList.addString(jsonArray.getString(i));
            }
        } else if (realmList.clazz == Byte[].class) {
            for (int i = 0; i < arraySize; i++) {
                osList.addBinary(JsonUtils.stringToBytes(jsonArray.getString(i)));
            }
        } else if (realmList.clazz == Date.class ) {
            for (int i = 0; i < arraySize; i++) {
                if (jsonArray.isNull(i)) {
                   osList.addNull();
                   continue;
                }

                Object timestamp = jsonArray.get(i);
                if (timestamp instanceof String) {
                    osList.addDate(JsonUtils.stringToDate((String) timestamp));
                } else {
                    osList.addDate(new Date(jsonArray.getLong(i)));
                }
            }
        } else if (realmList.clazz == Long.class || realmList.clazz == Integer.class ||
                realmList.clazz == Short.class || realmList.clazz == Byte.class) {
            for (int i = 0; i < arraySize; i++) {
                if (jsonArray.isNull(i)) {
                    osList.addNull();
                } else {
                    osList.addLong(jsonArray.getLong(i));
                }
            }
        } else {
            throwWrongElementType(realmList.clazz);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    static <E> void setRealmListWithJsonStream(RealmList<E> realmList, JsonReader jsonReader) throws IOException {
        OsList osList = realmList.getOsList();

        if (jsonReader.peek() == null) {
            jsonReader.skipValue();
            osList.removeAll();
            return;
        }

        jsonReader.beginArray();
        osList.removeAll();

        if (realmList.clazz == Boolean.class) {
            while (jsonReader.hasNext()) {
                if (jsonReader.peek() == null) {
                    jsonReader.skipValue();
                    osList.addNull();
                } else {
                    osList.addBoolean(jsonReader.nextBoolean());
                }
            }
        } else if (realmList.clazz == Float.class) {
            while (jsonReader.hasNext()) {
                if (jsonReader.peek() == null) {
                    jsonReader.skipValue();
                    osList.addNull();
                } else {
                    osList.addFloat((float) jsonReader.nextDouble());
                }
            }
        } else if (realmList.clazz == Double.class) {
            while (jsonReader.hasNext()) {
                if (jsonReader.peek() == null) {
                    jsonReader.skipValue();
                    osList.addNull();
                } else {
                    osList.addDouble(jsonReader.nextDouble());
                }
            }
        } else if (realmList.clazz == String.class) {
            while (jsonReader.hasNext()) {
                osList.addString(jsonReader.nextString());
            }
        } else if (realmList.clazz == Byte[].class) {
            while (jsonReader.hasNext()) {
                osList.addBinary(JsonUtils.stringToBytes(jsonReader.nextString()));
            }
        } else if (realmList.clazz == Date.class) {
            while (jsonReader.hasNext()) {
                JsonToken token = jsonReader.peek();
                if (token == JsonToken.NULL) {
                    jsonReader.skipValue();
                    osList.addNull();
                } else if (token == JsonToken.NUMBER) {
                    osList.addDate(new Date(jsonReader.nextLong()));
                } else {
                    osList.addDate(JsonUtils.stringToDate(jsonReader.nextString()));
                }
            }
        } else if (realmList.clazz == Long.class || realmList.clazz == Integer.class ||
                realmList.clazz == Short.class || realmList.clazz == Byte.class) {
            while (jsonReader.hasNext()) {
                if (jsonReader.peek() == null) {
                    jsonReader.skipValue();
                    osList.addNull();
                } else {
                    osList.addLong(jsonReader.nextLong());
                }
            }
        } else {
            throwWrongElementType(realmList.clazz);
        }

        jsonReader.endArray();
    }
}

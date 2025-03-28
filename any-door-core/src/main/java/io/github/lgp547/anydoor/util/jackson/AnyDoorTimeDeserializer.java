/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package io.github.lgp547.anydoor.util.jackson;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import io.github.lgp547.anydoor.util.LambdaUtil;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class AnyDoorTimeDeserializer extends JsonDeserializer<LocalDateTime> {
    
    public static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public static final LocalDateTimeDeserializer INSTANCE = new LocalDateTimeDeserializer(DATETIME_FORMAT);
    
    @Override
    public LocalDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        LocalDateTime dateTime;
        String text = jsonParser.getText();
        
        dateTime = LambdaUtil.runNotExc(() -> LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(text)), ZoneId.systemDefault()));
        if (null == dateTime) {
            dateTime = LambdaUtil.runNotExc(() -> getLocalDateTime(jsonParser, deserializationContext, INSTANCE));
        }
        if (null == dateTime) {
            dateTime = LambdaUtil.runNotExc(() -> getLocalDateTime(jsonParser, deserializationContext, LocalDateTimeDeserializer.INSTANCE));
        }
        if (null == dateTime) {
            dateTime = LambdaUtil.runNotExc(() -> LocalDateTime.parse(text + " 00:00:00", AnyDoorTimeDeserializer.DATETIME_FORMAT));
        }
        return dateTime;
    }
    
    private LocalDateTime getLocalDateTime(JsonParser jsonParser, DeserializationContext deserializationContext, LocalDateTimeDeserializer instance) {
        try {
            return instance.deserialize(jsonParser, deserializationContext);
        } catch (IOException e) {
            return null;
        }
    }
}

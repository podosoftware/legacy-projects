package com.podosoftware.competency.competency.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.podosoftware.competency.competency.CompetencyType;

public class CompetencyTypeDeserializer extends JsonDeserializer<CompetencyType> {

    public CompetencyTypeDeserializer() {
    }

    public CompetencyType deserialize(JsonParser jsonParser, DeserializationContext ctxt)
	    throws IOException, JsonProcessingException {
	ObjectCodec oc = jsonParser.getCodec();
	JsonNode node = oc.readTree(jsonParser);

	if (node == null)
	    return CompetencyType.NONE;

	CompetencyType type = CompetencyType.valueOf(node.get("competencyType").textValue().toUpperCase());
	return type;
    }

}

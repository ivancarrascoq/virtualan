package io.virtualan.core.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.virtualan.core.model.ContentType;
import io.virtualan.core.model.RequestType;
import io.virtualan.mapson.exception.BadInputDataException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import io.virtualan.core.model.VirtualServiceKeyValue;
import io.virtualan.core.model.VirtualServiceRequest;
import io.virtualan.entity.VirtualServiceEntity;
import org.springframework.xml.transform.StringSource;

@Component("converter")
public class Converter {

    private static final String PARAM_DELIMITER = ":_:";

    private String getString(Object jsonObject) throws JsonProcessingException {
        if(jsonObject != null && jsonObject instanceof LinkedHashMap) {
            String json = new ObjectMapper().writeValueAsString(jsonObject);
            return json;
        }
        return null;
    }


    private Object getJson(String jsonStr)  {
        if(jsonStr != null && !jsonStr.isEmpty()) {
            try {
                return new ObjectMapper().readValue(jsonStr, new TypeReference<Map<String, Object>>(){});
            } catch (JsonProcessingException e) {
                throw new BadDataException(e.getMessage());
            }
        }
        return null;
    }


    public void convertJsonAsString(VirtualServiceRequest virtualServiceRequest)
        throws JsonProcessingException {
        if(ContentType.JSON.equals(virtualServiceRequest.getContentType())){
            virtualServiceRequest.setInput(getString(virtualServiceRequest.getInput()));
            virtualServiceRequest.setOutput(getString(virtualServiceRequest.getOutput()));
        }
    }

    public VirtualServiceRequest convertAsJson(
        VirtualServiceRequest virtualServiceRequest) {
        VirtualServiceRequest virtualServiceRequestRes = new VirtualServiceRequest();
        BeanUtils.copyProperties(virtualServiceRequest, virtualServiceRequestRes);
        if(ContentType.JSON.equals(virtualServiceRequest.getContentType())){
            virtualServiceRequestRes.setInput(getJson(virtualServiceRequest.getInput() != null ? virtualServiceRequest.getInput().toString() : null));
            virtualServiceRequestRes.setOutput(getJson(virtualServiceRequest.getOutput() != null ? virtualServiceRequest.getOutput().toString() : null));
        }
        return virtualServiceRequestRes;
    }


    public static Map<String, String> converter(List<VirtualServiceKeyValue> paramList) {
        final Map<String, String> mapkeyValue = new HashMap<>();
        if (paramList != null && paramList.size() > 0) {
            for (final VirtualServiceKeyValue availableParam : paramList) {
                if (availableParam.getValue() != null) {
                    mapkeyValue.put(availableParam.getKey(), availableParam.getValue());
                }
            }
        }
        return mapkeyValue;
    }

    public static VirtualServiceRequest converterEToR(VirtualServiceEntity mockEntity) {
        final VirtualServiceRequest request = new VirtualServiceRequest();
        BeanUtils.copyProperties(mockEntity, request);
        if(mockEntity.getContentType() != null) {
            request.setContentType(ContentType.valueOf(mockEntity.getContentType()));
        }
        request.setInput(mockEntity.getInput());
        request.setOutput(mockEntity.getOutput());
        request.setAvailableParams(Converter.readParameter(mockEntity.getAvailableParamsList()));
        request.setHeaderParams(Converter.readParameter(mockEntity.getHeaderParamsList()));
        return request;
    }

    public static List<VirtualServiceKeyValue> readParameter(String paramStr) {
        final List<VirtualServiceKeyValue> availableParams = new LinkedList<>();
        if (paramStr != null) {
            final String[] availableParamsList = paramStr.split(Converter.PARAM_DELIMITER);
            if (availableParamsList != null && availableParamsList.length > 0) {
                for (final String availableParamsStr : availableParamsList) {
                    if (availableParamsStr.split("=").length == 2) {
                        availableParams
                            .add(new VirtualServiceKeyValue(availableParamsStr.split("=")[0],
                                availableParamsStr.split("=")[1]));
                    }
                }
            }
        }
        return availableParams;
    }

    public static VirtualServiceEntity converterRToE(VirtualServiceRequest mockRequest) {
        final VirtualServiceEntity mockEntity = new VirtualServiceEntity();
        BeanUtils.copyProperties(mockRequest, mockEntity);

        if(mockRequest.getContentType() != null) {
            mockEntity.setContentType(mockRequest.getContentType().name());
        }
        mockEntity.setInput(mockRequest.getInput() != null ? mockRequest.getInput().toString() : null);
        mockEntity.setOutput(mockRequest.getOutput() != null ? mockRequest.getOutput().toString() : null);

        mockEntity
            .setAvailableParamsList(Converter.readParameters(mockRequest.getAvailableParams()));
        mockEntity.setHeaderParamsList(Converter.readParameters(mockRequest.getHeaderParams()));
        return mockEntity;
    }

    public static String readParameters(List<VirtualServiceKeyValue> paranList) {
        final StringBuffer availableParamList = new StringBuffer();
        String availableParamStr = null;
        if (paranList != null && paranList.size() > 0) {
            for (final VirtualServiceKeyValue availableParam : paranList) {
                if (availableParam.getValue() != null) {
                    availableParamList.append(availableParam.getKey() + "="
                        + availableParam.getValue() + Converter.PARAM_DELIMITER);
                }
            }
            availableParamStr = availableParamList.toString();
            if (availableParamStr.lastIndexOf(Converter.PARAM_DELIMITER) > 0) {
                return availableParamStr.substring(0,
                    availableParamStr.lastIndexOf(Converter.PARAM_DELIMITER));
            } else if (availableParamStr != null && availableParamStr.trim().length() > 0) {
                return availableParamStr;
            }
        }
        return availableParamStr;
    }
}

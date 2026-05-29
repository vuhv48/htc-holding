package dbputilities.addressservices;

import com.konylabs.middleware.api.ServicesManager;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import org.mockito.Answers;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class Main {

    public static void main(String[] args) throws Exception {
        DataControllerRequest request = mock(DataControllerRequest.class);
        DataControllerResponse response = mock(DataControllerResponse.class);

        // RETURNS_DEEP_STUBS tu dong tao mock cho tung buoc trong chuoi builder
        ServicesManager servicesManager = mock(ServicesManager.class, Answers.RETURNS_DEEP_STUBS);

        when(request.getServicesManager()).thenReturn(servicesManager);
        when(request.getHeaderMap()).thenReturn(new HashMap<>());

        String fakeJson = "{\"records\":[{\"AddressType\":\"Home\",\"AddressLine1\":\"123 Main St\",\"City\":\"Springfield\"}]}";

        when(servicesManager
                .getRequestBuilder(any())
                .withInputs(any())
                .withHeaders(any())
                .build()
                .invokeServiceAndGetJson())
                .thenReturn(fakeJson);

        GetAllAddress service = new GetAllAddress();
        Object result = service.invoke("getAllAddress", new Object[]{}, request, response);

        System.out.println("Result: " + result);
    }
}
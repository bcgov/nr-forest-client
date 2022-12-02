package ca.bc.gov.app.m.ob.service.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import ca.bc.gov.app.core.util.CoreUtil;
import ca.bc.gov.app.m.ob.service.OrgBookApiService;
import ca.bc.gov.app.m.ob.vo.OrgBookResponseVO;
import ca.bc.gov.app.m.oracle.legacyclient.entity.ClientDoingBusinessAsEntity;
import ca.bc.gov.app.m.oracle.legacyclient.entity.ForestClientEntity;
import ca.bc.gov.app.m.oracle.legacyclient.repository.ClientDoingBusinessAsRepository;
import ca.bc.gov.app.m.oracle.legacyclient.repository.ForestClientRepository;
import ca.bc.gov.app.m.oracle.legacyclient.vo.ClientPublicViewVO;

@Service(OrgBookApiService.BEAN_NAME)
public class OrgBookApiServiceImpl implements OrgBookApiService {
    
    public static final Logger logger = LoggerFactory.getLogger(OrgBookApiServiceImpl.class);

    @Inject
    private CoreUtil coreUtil;
    
    @Inject
    private ClientDoingBusinessAsRepository clientDoingBusinessAsRepository;
    
    @Inject
    private ForestClientRepository forestClientRepository;
    
    @Override
    public OrgBookResponseVO findByClientName(String clientName) {
        String uri;
		try {
			uri = "https://orgbook.gov.bc.ca/api/v3/search/autocomplete?q=" +
			      URLEncoder.encode(clientName, "utf-8");
		}
		catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Failed to encode url parameter " + e.toString());
        }
        
		RestTemplate restTemplate = new RestTemplate();
        String restCallResponse = restTemplate.getForObject(toURI(uri), String.class);
        
        OrgBookResponseVO response = coreUtil.jsonStringToObj(restCallResponse, OrgBookResponseVO.class);
        
        logger.info("Results: " + response.results);

        return response;
    }
    
    @Override
    public ResponseEntity<Object> findByIncorporationNumber(String incorporationNumber) {
        String url = "https://orgbook.gov.bc.ca/api/v4/search/topic?format=json&inactive=any&latest=true&ordering=-score&q=" +
                      incorporationNumber + "&revoked=false";
        RestTemplate restTemplate = new RestTemplate();
        String restCallResponse = restTemplate.getForObject(toURI(url), String.class);
        
        OrgBookResponseVO response = coreUtil.jsonStringToObj(restCallResponse, OrgBookResponseVO.class);
        
        logger.info("Response: " + response.toString());
        logger.info("Results: " + response.results);

        return ResponseEntity.ok(response);
    }

    private URI toURI(String uri) {
        try {
            return new URI(uri);
        }
        catch (URISyntaxException e) {
            throw new IllegalArgumentException("Failed to convert input value to URI. " + e.toString());
        }
    }
    
    @Override
	public List<ClientDoingBusinessAsEntity> validateClientDoingBusinessAs() {
		
		Date startedDate = new Date();
		
		Long count = clientDoingBusinessAsRepository.countAll();
		List<ClientDoingBusinessAsEntity> doingBusinessClients = new ArrayList<>();
		List<ClientPublicViewVO> clients = new ArrayList<>();
		
		int take = 10;
	    int numberOfPages = (int) Math.ceil(count/take);
	    
		for (int page = 1; page <= numberOfPages; page++) {
			doingBusinessClients.addAll(clientDoingBusinessAsRepository.findAllPagable(PageRequest.of(page, take)));
		}
		
		for (ClientDoingBusinessAsEntity clientDoingBusinessAsEntity : doingBusinessClients) {
			OrgBookResponseVO orgBookResponseVO = this.findByClientName(
													clientDoingBusinessAsEntity.getDoingBusinessAsName());
			
			ClientPublicViewVO client = new ClientPublicViewVO();
			client.clientNumber = clientDoingBusinessAsEntity.getClientNumber();
			client.clientName = clientDoingBusinessAsEntity.getDoingBusinessAsName();
			
			if (orgBookResponseVO.results.size() > 0) {
				client.clientNameInOrgBook = orgBookResponseVO.results.get(0).value;
				client.sameName = client.clientNameInOrgBook.equals(client.clientName);
			}
			
			clients.add(client);
		}
		
		//logger.info(coreUtil.objToJsonString(clients));
		String content = coreUtil.objToJsonString(clients);
		String path = "C:/repo/a.txt";
		try {
			Files.write( Paths.get(path), content.getBytes());
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		
		logger.info("Started at " + startedDate);
		logger.info("Finished at " + new Date());
		return doingBusinessClients;
	}

	@Override
	public List<ForestClientEntity> validateUnregisteredCompanies() {
		Date startedDate = new Date();
		
		Long count = forestClientRepository.countAll();
		List<ForestClientEntity> unregisteredCompanies = new ArrayList<>();
		List<ClientPublicViewVO> clients = new ArrayList<>();
		
		int take = 10;
	    int numberOfPages = (int) Math.ceil(count/take);
	    
		for (int page = 1; page <= numberOfPages; page++) {
			unregisteredCompanies.addAll(forestClientRepository.findAllPagable(PageRequest.of(page, take)));
		}
		
		for (ForestClientEntity forestClientEntity : unregisteredCompanies) {
			OrgBookResponseVO orgBookResponseVO = this.findByClientName(forestClientEntity.getClientName());
			
			ClientPublicViewVO client = new ClientPublicViewVO();
			client.clientNumber = forestClientEntity.getClientNumber();
			client.clientName = forestClientEntity.getClientName();
			
			if (orgBookResponseVO.results.size() > 0) {
				client.clientNameInOrgBook = orgBookResponseVO.results.get(0).value;
				client.sameName = client.clientNameInOrgBook.equals(client.clientName);
			}
			
			clients.add(client);
		}
		
		String content = coreUtil.objToJsonString(clients);
		String path = "C:/repo/a.txt";
		try {
			Files.write( Paths.get(path), content.getBytes());
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		
		logger.info("Started at " + startedDate);
		logger.info("Finished at " + new Date());
		return unregisteredCompanies;
	}
    
}

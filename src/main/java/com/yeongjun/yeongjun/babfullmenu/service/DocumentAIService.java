package com.yeongjun.yeongjun.babfullmenu.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.gax.rpc.ApiException;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.documentai.v1.*;
import com.google.protobuf.util.JsonFormat;
import com.yeongjun.yeongjun.babfullmenu.entity.MenuDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class DocumentAIService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentAIService.class);

    @Value("${gcloud.project-id}")
    private String projectId;

    @Value("${gcloud.location}")
    private String location;

    @Value("${gcloud.processor-id}")
    private String processorId;

    @Value("${gcloud.processor-version}")
    private String processorVersion;

    @Value("${gcloud.credentials-file}")
    private String credentialsFilePath;

    private final ObjectMapper objectMapper;

    /**
     * 업로드된 MultipartFile 문서를 Google Cloud Document AI로 처리하여 MenuDTO로 변환
     */
    public MenuDTO processDocumentToMenuDTO(MultipartFile file) throws IOException {

        // Jackson의 최대 문자열 길이 제한을 늘리기
        objectMapper.getFactory().setStreamReadConstraints(
                StreamReadConstraints.builder().maxStringLength(Integer.MAX_VALUE).build()
        );

        // 1) DocumentProcessorService 클라이언트 설정
        DocumentProcessorServiceSettings.Builder builder =
                DocumentProcessorServiceSettings.newBuilder();

        if (credentialsFilePath != null && !credentialsFilePath.isEmpty()) {
            // 수정된 부분: String을 바이트로 바꾸는 것이 아니라, 실제 파일을 엽니다.
            try (InputStream keyStream = new FileInputStream(credentialsFilePath)) {
                GoogleCredentials credentials = GoogleCredentials.fromStream(keyStream);
                builder.setCredentialsProvider(FixedCredentialsProvider.create(credentials));
            } catch (FileNotFoundException e) {
                logger.error("JSON 키 파일을 찾을 수 없습니다. 경로를 확인하세요: {}", credentialsFilePath);
                throw e;
            }
        }

        DocumentProcessorServiceSettings settings = builder.build();

        // 2) DocumentProcessorServiceClient 생성
        try (DocumentProcessorServiceClient client = DocumentProcessorServiceClient.create(settings)) {

            // Processor Version 이름 설정
            String processorVersionName = String.format(
                    "projects/%s/locations/%s/processors/%s/processorVersions/%s",
                    projectId, location, processorId, processorVersion
            );

            // 3) RawDocument 생성 (MultipartFile → ByteString)
            RawDocument rawDocument = RawDocument.newBuilder()
                    .setContent(com.google.protobuf.ByteString.copyFrom(file.getBytes()))
                    .setMimeType(file.getContentType()) // ex) "application/pdf", "image/png"
                    .build();

            // 4) 요청 빌드
            ProcessRequest request = ProcessRequest.newBuilder()
                    .setName(processorVersionName)
                    .setRawDocument(rawDocument)
                    .build();

            // 5) Document AI API 호출
            ProcessResponse response = client.processDocument(request);
            Document document = response.getDocument();

            // 6) Protobuf Document를 JSON으로 변환 (메모리 최적화)
            StringWriter writer = new StringWriter();
            JsonFormat.printer().appendTo(document, writer);
            JsonNode rootNode = objectMapper.readTree(writer.toString());

            // 7) JSON 파싱하여 'entities' 추출
            JsonNode entitiesNode = rootNode.path("entities");

            if (entitiesNode.isMissingNode() || !entitiesNode.isArray()) {
                logger.warn("'entities' 필드가 존재하지 않거나 배열이 아닙니다.");
                return new MenuDTO(); // 빈 MenuDTO 반환
            }

            // 8) MenuDTO 객체 초기화
            MenuDTO menuDTO = new MenuDTO();

            // 9) 엔티티 순회 및 MenuDTO 매핑
            for (JsonNode entityNode : entitiesNode) {
                String type = entityNode.path("type").asText();
                String mentionText = entityNode.path("mentionText").asText();

                JsonNode propertiesNode = entityNode.path("properties");
                if (propertiesNode.isArray() && !propertiesNode.isEmpty()) {
                    for (JsonNode subEntityNode : propertiesNode) {
                        String subType = subEntityNode.path("type").asText();
                        String subMentionText = subEntityNode.path("mentionText").asText();
                        mapSubEntityToMenuDTO(menuDTO, subType, subMentionText);
                    }
                } else {
                    mapEntityToMenuDTO(menuDTO, type, mentionText);
                }
            }

            logger.info("Mapped MenuDTO: {}", menuDTO);
            return menuDTO;
        } catch (ApiException e) {
            logger.error("Document AI API 오류: {}", e.getStatusCode(), e);
            throw new IOException("Document AI API 오류가 발생했습니다.", e);
        } catch (Exception e) {
            logger.error("예상치 못한 오류: {}", e.getMessage(), e);
            throw new IOException("문서 처리 중 예상치 못한 오류가 발생했습니다.", e);
        }
    }

    /**
     * 상위 엔티티를 MenuDTO에 매핑하는 메서드
     */
    private void mapEntityToMenuDTO(MenuDTO menuDTO, String type, String mentionText) {
        switch (type.toLowerCase()) {
            case "start_dt":
                menuDTO.setStart_dt(mentionText);
                break;
            case "end_dt":
                menuDTO.setEnd_dt(mentionText);
                break;
            case "is_menu":
                menuDTO.setIs_menu(mentionText);
                break;
            case "lunch":
            case "morning":
                logger.debug("Skipping parent entity type: {}", type);
                break;
            default:
                logger.info("Unhandled entity type: {}", type);
                break;
        }
    }

    /**
     * 하위 엔티티를 MenuDTO에 매핑하는 메서드
     */
    private void mapSubEntityToMenuDTO(MenuDTO menuDTO, String type, String mentionText) {
        switch (type.toLowerCase()) {
            case "morning_day1":
                menuDTO.getMorning_day1().add(mentionText);
                break;
            case "morning_day2":
                menuDTO.getMorning_day2().add(mentionText);
                break;
            case "morning_day3":
                menuDTO.getMorning_day3().add(mentionText);
                break;
            case "morning_day4":
                menuDTO.getMorning_day4().add(mentionText);
                break;
            case "morning_day5":
                menuDTO.getMorning_day5().add(mentionText);
                break;
            case "lunch_day1":
                menuDTO.getLunch_day1().add(mentionText);
                break;
            case "lunch_day2":
                menuDTO.getLunch_day2().add(mentionText);
                break;
            case "lunch_day3":
                menuDTO.getLunch_day3().add(mentionText);
                break;
            case "lunch_day4":
                menuDTO.getLunch_day4().add(mentionText);
                break;
            case "lunch_day5":
                menuDTO.getLunch_day5().add(mentionText);
                break;
            case "lunch_name":
                menuDTO.getLunch_names().add(mentionText);
                break;
            default:
                logger.info("Unhandled sub-entity type: {}", type);
                break;
        }
    }
}

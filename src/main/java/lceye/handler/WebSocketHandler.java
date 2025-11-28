package lceye.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class WebSocketHandler extends TextWebSocketHandler {

    // [*] 접속자 목록
    private final ConcurrentHashMap<Integer, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();

    // 소켓 연결
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("클라이언트와 연결 시작");
    }// f end

    // 메시지를 받았을때
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        try{
            // 1. 메시지를 JSON으로 파싱
            JsonNode node = mapper.readTree(payload);
            // 2. 타입 확인
            if (node.has("type") && "message".equals(node.get("type").asText()) ){
                // mno 추출
                if (node.has("mno")){
                    int mno = node.get("mno").asInt();
                    // 세션 속성에 저장
                    session.getAttributes().put("mno",mno);
                    // 관리용 변수에 저장
                    sessions.put(mno,session);
                }else { // mno 존재하지않으면 세션 닫기
                    session.close();
                }// if end
            }//if end
        }catch (Exception e){
            e.printStackTrace();
        }// try end
    }// f end


    /**
     * 클라이언트에게 메시지 전송
     *
     * @param mno 회원번호
     * @param result 매칭된 데이터
     * @author 민성호
     */
    public void sendMessage(int mno , Map<String, Set<String>> result){
        WebSocketSession session = sessions.get(mno);
        if (session != null && session.isOpen()){
            try{
                Map<String,Object> messageMap = new HashMap<>();
                messageMap.put("type","gemini");
                messageMap.put("data",result);
                // 문자열로 파싱
                String jsonPayload = mapper.writeValueAsString(messageMap);
                // 메시지 전송
                session.sendMessage(new TextMessage(jsonPayload));
                System.out.println("메시지 전송");
            }catch (Exception e){
                e.printStackTrace();
            }// try end
        }// if end
    }// f end

    // 소켓 연결종료
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("클라이언트와 연결 종료");
        // 세션 목록에서 제거
        Object pjNumber = session.getAttributes().get("pjno");
        if (pjNumber != null && pjNumber instanceof Integer){
            int pjno = (int) pjNumber;
            sessions.remove(pjno);
        }//if end
    }// f end
}// class end

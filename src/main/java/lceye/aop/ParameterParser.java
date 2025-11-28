package lceye.aop;

import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class ParameterParser {
    /**
     * SpEL 표현식을 파싱하여 실제 값을 추출하는 메소드
     *
     * @param parameters 메소드의 파라미터 이름 배열 (예: ["pjno", "token"])
     * @param args       메소드의 실제 인자값 배열 (예: ["300001", "token 내용"])
     * @param key        변환할 SpEL 문자열 (예: "#pjno")
     * @return SpEL을 통해 추출된 실제 값 (Object 타입)
     * @author AhnJH
     */
    public static Object getDynamicValue(String[] parameters, Object[] args, String key){
        // 1. SpEL 파서 생성 : 문자열로 된 표현식을 이해하고 해석할 수 있음
        ExpressionParser parser = new SpelExpressionParser();
        // 2. SpEL 컨텍스트 생성 : 파서가 표현식을 해석할 때 참고할 변수 저장소
        StandardEvaluationContext context = new StandardEvaluationContext();
        // 3. 매개변수 이름과 실제 값을 매핑하여 컨텍스트에 저장
        for (int i = 0; i < parameters.length; i++){
            context.setVariable(parameters[i], args[i]);
        } // for end
        // 4. 실제 파싱 및 값 추출
        return parser.parseExpression(key).getValue(context, Object.class);
    } // func end
} // class end
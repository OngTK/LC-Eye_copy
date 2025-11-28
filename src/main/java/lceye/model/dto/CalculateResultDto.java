package lceye.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalculateResultDto {

    private int fno;            // 흐름번호(FK)
    private String fuuid;       // 흐름 uuid
    private String fname;       // 흐름명
    private double amount;      // 양
    private int uno;            // 소분류단위번호 // 40111
    private String uname;       // 소분류 단위명  // kg
    private boolean isInput;    // true : Input / False : Output

} // class end

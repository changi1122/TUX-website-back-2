package kr.ac.cbnu.tux.domain.admin.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ac.cbnu.tux.domain.user.dto.response.UserResponse;
import kr.ac.cbnu.tux.domain.user.enums.UserRole;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "관리자 기능(admin)", description = "관리자 API")
public interface AdminControllerDocs {

    @Operation(method = "GET", summary = "승인 대기 회원 목록 조회", description = "승인 대기 회원 목록을 조회합니다. (GUEST)")
    List<UserResponse> listAllGuest();

    @Operation(method = "GET", summary = "회원 목록 조회", description = "승인된 회원 목록을 조회합니다. (USER, MANAGER, ADMIN)")
    List<UserResponse> listAllMemberNotGuest();

    @Operation(method = "POST", summary = "회원 등급 변경", description = "회원 등급을 변경합니다.")
    void changeUserRole(@PathVariable Long id, @PathVariable UserRole role);

    @Operation(method = "PUT", summary = "임시 비밀번호 설정", description = "관리자 권한으로 회원의 비밀번호를 변경합니다.")
    void setTemporalPassword(@PathVariable Long id, @RequestBody String password);

    @Operation(method = "DELETE", summary = "회원 강제(Forced) 삭제", description = "회원을 DB에서 강제로 삭제합니다. 연관 관계가 있는 모든 데이터를 삭제해야 합니다.")
    void deleteUserHardly(@PathVariable Long id);

    @Operation(method = "DELETE", summary = "로그인 밴", description = "회원을 로그인할 수 없도록 설정합니다.")
    void ban(@PathVariable Long id);
}

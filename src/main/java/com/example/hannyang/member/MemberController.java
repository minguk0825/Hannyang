package com.example.hannyang.member;

import com.example.hannyang.jwt.JwtTokenProvider;
import com.example.hannyang.member.dtos.MemberRequestDto;
import com.example.hannyang.member.dtos.MemberResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "member", description = "회원 API")
public class MemberController {
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 회원정보 조회 API
     */
    @Operation(summary = "회원 정보 조회", description = "사용자의 상세 정보를 조회합니다.", tags = {"member"})
    @ApiResponse(responseCode = "200", description = "성공적으로 회원 정보를 조회함",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = MemberResponseDto.class)))
    @ApiResponse(responseCode = "401", description = "인증 실패")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    @GetMapping("/api/v1/member")
    public ResponseEntity<?> findMember(@RequestHeader("Authorization") String accessToken) {
        Long id = this.jwtTokenProvider.getUserIdFromToken(accessToken.substring(7));
        MemberResponseDto memberResponseDto = this.memberService.findById(id);
        return ResponseEntity.status(HttpStatus.OK).body(memberResponseDto);
    }

    /**
     * 회원정보 수정 API
     */
    @Operation(summary = "회원 정보 수정", description = "사용자의 정보를 수정합니다.", tags = {"member"})
    @ApiResponse(responseCode = "200", description = "성공적으로 회원 정보를 수정함")
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @ApiResponse(responseCode = "401", description = "인증 실패")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    @PutMapping("/api/v1/member")
    public ResponseEntity<?> updateMember(@RequestHeader("Authorization") String accessToken,
                                          @RequestBody MemberRequestDto requestDto) {
        Long id = this.jwtTokenProvider.getUserIdFromToken(accessToken.substring(7));
        this.memberService.update(id, requestDto);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    /**
     * 회원정보 삭제 API
     */
    @Operation(summary = "회원 정보 삭제", description = "사용자의 정보를 삭제합니다.", tags = {"member"})
    @ApiResponse(responseCode = "200", description = "성공적으로 회원 정보를 삭제함")
    @ApiResponse(responseCode = "401", description = "인증 실패")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    @DeleteMapping("/api/v1/member")
    public ResponseEntity<?> deleteMember(@RequestHeader("Authorization") String accessToken) {
        Long id = this.jwtTokenProvider.getUserIdFromToken(accessToken.substring(7));
        this.memberService.delete(id);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }
}

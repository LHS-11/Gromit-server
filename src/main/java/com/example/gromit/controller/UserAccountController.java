package com.example.gromit.controller;

import com.example.gromit.base.BaseResponse;
import com.example.gromit.dto.user.request.ChangeNicknameRequestDto;
import com.example.gromit.dto.user.request.SignUpRequestDto;
import com.example.gromit.dto.user.response.GithubNicknameResponseDto;
import com.example.gromit.dto.user.response.NicknameResponseDto;
import com.example.gromit.dto.user.response.SignUpResponseDto;
import com.example.gromit.entity.UserAccount;
import com.example.gromit.service.UserAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;

import static com.example.gromit.exception.ErrorCode.DUPLICATED_NICKNAME;


@Slf4j
@RequiredArgsConstructor
@Validated
@RequestMapping("/users")
@RestController
public class UserAccountController {

    private final UserAccountService userAccountService;


    /**
     * 회원가입 로직
     *
     * @param signUpRequestDto
     * @param bindingResult
     * @return
     */
    @PostMapping
    public BaseResponse<SignUpResponseDto> signUp(@Validated @RequestBody SignUpRequestDto signUpRequestDto, BindingResult bindingResult) {
        log.info("sign-up");

        if (bindingResult.hasErrors()) {
            ObjectError objectError = bindingResult.getAllErrors().stream().findFirst().get();
            return BaseResponse.onFailure(400, objectError.getDefaultMessage(), null);
        }

        // 회원 가입 비즈니스 로직
        SignUpResponseDto result = userAccountService.signUp(signUpRequestDto);

        return BaseResponse.onSuccess(result);
    }

    /**
     * 깃허브 닉네임 조회 API
     */
    @GetMapping(value = {"/github/{nickname}","/github"})
    public BaseResponse<GithubNicknameResponseDto> checkGithubNickname(@PathVariable(value = "nickname",required = false)
                                                                           @NotBlank(message = "깃허브 닉네임을 입력해주세요.") String githubNickname) {
        log.info(githubNickname);
        GithubNicknameResponseDto result = userAccountService.getGithubUser(githubNickname);
        return BaseResponse.onSuccess(result);
    }

    /**
     * 닉네임 조회 API
     */
    @GetMapping(value={"/check/{nickname}","/check"})
    public BaseResponse<NicknameResponseDto> checkNickname(
            @PathVariable(value = "nickname",required = false)
            @NotBlank(message = "닉네임을 입력해주세요.")
            @Pattern(regexp = "^[0-9a-zA-Z가-힣]{1,8}", message = "닉네임은 8자이하 한글,숫자,영어로만 이루어져야 합니다.") String nickname) {
        log.info(nickname);
        userAccountService.validateNickname(nickname);
        NicknameResponseDto result = NicknameResponseDto.of(nickname);
        return BaseResponse.onSuccess(result);
    }

    @DeleteMapping
    public BaseResponse<String> deleteUserAccount(@AuthenticationPrincipal UserAccount userAccount) {
        userAccountService.delete(userAccount);
        return BaseResponse.onSuccess("회원 탈퇴 성공했습니다.");
    }

    /**
     * 깃허브 커밋 조회 API
     */
    @PatchMapping("/reload") //커밋 새로고침
    public BaseResponse<String> reloadCommits(@AuthenticationPrincipal UserAccount userAccount) {
        System.out.println("커밋 새로고침 컨트롤러");
        userAccountService.reloadCommits(userAccount, LocalDate.now());
        return BaseResponse.onSuccess("커밋 새로고침에 성공했습니다.");
    }

    /**
     * 닉네임 변경 API
     */
    @PatchMapping("/change/nickname")
    public BaseResponse<NicknameResponseDto> changeNickname(@AuthenticationPrincipal UserAccount userAccount,
                                               @Validated @RequestBody ChangeNicknameRequestDto changeNicknameRequestDto,BindingResult bindingResult){

        if(bindingResult.hasErrors()){
            ObjectError objectError = bindingResult.getAllErrors().stream().findFirst().get();
            return BaseResponse.onFailure(400, objectError.getDefaultMessage(), null);
        }

        NicknameResponseDto result = userAccountService.changeNickname(userAccount, changeNicknameRequestDto);
        return BaseResponse.onSuccess(result);
    }


}

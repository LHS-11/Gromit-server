package com.example.gromit.controller;

import com.example.gromit.dto.home.response.GetCollectionResponse;
import com.example.gromit.dto.home.response.ShowHomeResponse;
import com.example.gromit.entity.UserAccount;
import com.example.gromit.entity.UserCharacter;
import com.example.gromit.service.JwtService;
import com.example.gromit.service.UserAccountService;
import com.example.gromit.service.UserCharacterService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(HomeController.class)
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserAccountService userAccountService;

    @MockBean
    private UserCharacterService userCharacterService;

    @DisplayName("Home API 성공 테스트")
    @Test
    @WithMockUser(username = "user")
    void 홈_조회_유저_캐릭터_존재_O_테스트() throws Exception {
        //given
        UserAccount userAccount = new UserAccount();
        userAccount.setId(1L);

        UserCharacter userCharacter = new UserCharacter();

        ShowHomeResponse showHomeResponse = ShowHomeResponse.of(1,1,1,"test","test",10);

        //when
        willDoNothing().given(userAccountService).reloadCommits(any(UserAccount.class),any(LocalDate.class));
        given(userCharacterService.findByUserAccountId(any(UserAccount.class))).willReturn(userCharacter);
        given(userCharacterService.reloadCharacter(any(UserAccount.class))).willReturn((Future<ShowHomeResponse>) showHomeResponse);

        mockMvc.perform(get("/home").with(user(userAccount)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.message").value("요청에 성공하였습니다."))
                .andExpect(jsonPath("$.result.commits").value(1))
                .andExpect(jsonPath("$.result.todayCommit").value(1))
                .andExpect(jsonPath("$.result.level").value(1))
                .andExpect(jsonPath("$.result.name").value("test"))
                .andExpect(jsonPath("$.result.img").value("test"))
                .andExpect(jsonPath("$.result.goal").value(10));


        //then
        verify(userAccountService,times(1)).reloadCommits(any(UserAccount.class),any(LocalDate.class));
        verify(userCharacterService, times(1)).findByUserAccountId(any(UserAccount.class));
        verify(userCharacterService,times(0)).grantFirstCharacter(userAccount);
        verify(userCharacterService, times(1)).reloadCharacter(any(UserAccount.class));
//        then(userAccountService).should(times(1)).reloadCommits(any(UserAccount.class),any(LocalDate.class));
    }

    @DisplayName("새로고침 API 테스트")
    @Test
    @WithMockUser(username = "user")
    void 새로고침_성공_테스트() throws Exception {
        //given
        UserAccount userAccount = new UserAccount();
        userAccount.setId(1L);

        ShowHomeResponse showHomeResponse = ShowHomeResponse.of(1,1,1,"test","test",10);


        //when
        willDoNothing().given(userAccountService).reloadCommits(any(UserAccount.class),any(LocalDate.class));
        given(userCharacterService.reloadCharacter(any(UserAccount.class))).willReturn((Future<ShowHomeResponse>) showHomeResponse);

        mockMvc.perform(get("/home/reload").with(user(userAccount)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.message").value("요청에 성공하였습니다."))
                .andExpect(jsonPath("$.result.commits").value(1))
                .andExpect(jsonPath("$.result.todayCommit").value(1))
                .andExpect(jsonPath("$.result.level").value(1))
                .andExpect(jsonPath("$.result.name").value("test"))
                .andExpect(jsonPath("$.result.img").value("test"))
                .andExpect(jsonPath("$.result.goal").value(10));

        //then
        verify(userAccountService, times(1)).reloadCommits(any(UserAccount.class), any(LocalDate.class));
        verify(userCharacterService, times(1)).reloadCharacter(any(UserAccount.class));
    }

    @DisplayName("컬렉션 조회 API 테스트")
    @Test
    @WithMockUser(username = "user")
    void 컬렉션_조회_성공_테스트() throws Exception {
        //given
        UserAccount userAccount = new UserAccount();
        userAccount.setId(1L);

        List<GetCollectionResponse> getCollectionResponseList = new ArrayList<>();
        getCollectionResponseList.add(GetCollectionResponse.of("test", "test"));
        getCollectionResponseList.add(GetCollectionResponse.of("test1", "test1"));

        //when
        given(userCharacterService.getCollection(any(UserAccount.class))).willReturn(getCollectionResponseList);

        mockMvc.perform(get("/home/collections")
                        .with(user(userAccount)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.message").value("요청에 성공하였습니다."))
                .andExpect(jsonPath("$.result[0].name").value("test"))
                .andExpect(jsonPath("$.result[0].img").value("test"))
                .andExpect(jsonPath("$.result[1].name").value("test1"))
                .andExpect(jsonPath("$.result[1].img").value("test1"));

        //then
        verify(userCharacterService, times(1)).getCollection(any(UserAccount.class));
    }

}
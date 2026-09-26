package com.vocal.app.global.security.oauth;

import com.vocal.app.global.enums.Gender;
import com.vocal.app.global.enums.Role;
import com.vocal.app.global.enums.SocialType;
import com.vocal.app.user.entity.User;
import com.vocal.app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String socialUid = String.valueOf(attributes.get("id"));

        @SuppressWarnings("unchecked")
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        @SuppressWarnings("unchecked")
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        String email = (String) kakaoAccount.get("email");
        String nickname = (String) profile.get("nickname");

        // 카카오가 내려주는 성별("male"/"female")을 우리 Gender enum으로 변환
        String kakaoGender = (String) kakaoAccount.get("gender");
        Gender gender = "male".equalsIgnoreCase(kakaoGender) ? Gender.MALE
                : "female".equalsIgnoreCase(kakaoGender) ? Gender.FEMALE
                  : null;

        User user = userRepository.findBySocialTypeAndSocialUid(SocialType.KAKAO, socialUid)
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .username("kakao_" + socialUid)
                                .email(email)
                                .name(nickname)
                                .nickname(nickname)
                                .gender(gender)
                                .passwordHash(UUID.randomUUID().toString())
                                .role(Role.USER)
                                .socialType(SocialType.KAKAO)
                                .socialUid(socialUid)
                                .build()
                ));

        return new OAuth2UserPrincipal(user, attributes);
    }
}
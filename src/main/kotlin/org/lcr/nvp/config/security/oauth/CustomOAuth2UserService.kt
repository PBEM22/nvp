package org.lcr.nvp.config.security.oauth

import org.lcr.nvp.domain.member.domain.User
import org.lcr.nvp.domain.member.repository.MemberRepository
import org.lcr.nvp.domain.member.repository.RoleRepository
import org.lcr.nvp.domain.member.repository.UserRepository
import org.lcr.nvp.global.exception.domain.RoleNotFoundException
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CustomOAuth2UserService(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val memberRepository: MemberRepository
) : DefaultOAuth2UserService() {

    @Transactional
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val oAuth2User = super.loadUser(userRequest)

        val registrationId = userRequest.clientRegistration.registrationId
        val userNameAttributeName = userRequest.clientRegistration.providerDetails.userInfoEndpoint.userNameAttributeName

        val oAuth2Attributes = OAuth2Attributes.of(registrationId, userNameAttributeName, oAuth2User.attributes)

        val user = saveOrUpdate(oAuth2Attributes)

        return DefaultOAuth2User(
            user.roles.map { org.springframework.security.core.authority.SimpleGrantedAuthority(it.roleName) },
            oAuth2User.attributes,
            userNameAttributeName
        )
    }

    private fun saveOrUpdate(attributes: OAuth2Attributes): User {
        val existingUser = userRepository.findByEmail(attributes.email)

        if (existingUser != null) {
            // 이메일이 존재하지만, 탈퇴한 회원인 경우 (계정 부활)
            if (existingUser.deletedAt != null) {
                existingUser.unDelete()
                // 소셜 로그인이므로 이름만 provider의 정보로 업데이트 (선택적)
                if (existingUser.name != attributes.name) {
                    existingUser.name = attributes.name
                }

                // 연관된 Member도 부활시킴
                memberRepository.findByUser(existingUser)?.apply {
                    this.unDelete()
                    this.membershipStatus = "ACTIVE_MEMBER"
                }
                return userRepository.save(existingUser)
            }
            // 활성 상태인 기존 회원이면 그냥 반환
            return existingUser
        }

        // 신규 사용자인 경우 provider의 정보로 생성
        val defaultRole = roleRepository.findByRoleName("ROLE_USER") ?: throw RoleNotFoundException()
        val newUser = User(
            email = attributes.email,
            name = attributes.name,
            password = null, // 소셜 로그인은 비밀번호 없음
            birthday = null, // 추가 정보는 null로 시작
            isMale = null, // 추가 정보는 null로 시작
            providerId = attributes.providerId,
            loginType = attributes.registrationId.uppercase()
        ).apply {
            this.roles.add(defaultRole)
        }
        return userRepository.save(newUser)
    }
}

data class OAuth2Attributes(
    val attributes: Map<String, Any>,
    val nameAttributeKey: String,
    val name: String,
    val email: String,
    val providerId: String,
    val registrationId: String
) {
    companion object {
        fun of(registrationId: String, userNameAttributeName: String, attributes: Map<String, Any>): OAuth2Attributes {
            return when (registrationId) {
                "kakao" -> ofKakao(userNameAttributeName, attributes)
                else -> throw IllegalArgumentException("Unsupported registrationId: $registrationId")
            }
        }

        @Suppress("UNCHECKED_CAST")
        private fun ofKakao(userNameAttributeName: String, attributes: Map<String, Any>): OAuth2Attributes {
            val kakaoAccount = attributes["kakao_account"] as Map<String, Any>
            val profile = kakaoAccount["profile"] as? Map<String, Any>
            val nickname = profile?.get("nickname") as? String

            // 카카오의 providerId는 일반적으로 최상위 attributes 맵의 'id' 필드에 해당합니다.
            val kakaoProviderId = attributes[userNameAttributeName]?.toString() ?: throw IllegalArgumentException("Kakao providerId (id) not found")

            return OAuth2Attributes(
                attributes = attributes,
                nameAttributeKey = userNameAttributeName,
                name = nickname ?: "사용자", // 닉네임이 없으면 "사용자"를 기본값으로 사용
                email = kakaoAccount["email"] as String,
                providerId = kakaoProviderId, // 올바르게 추출된 providerId 사용
                registrationId = "kakao"
            )
        }
    }
}


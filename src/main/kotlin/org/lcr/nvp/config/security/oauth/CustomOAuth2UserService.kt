package org.lcr.nvp.config.security.oauth

import org.lcr.nvp.domain.member.domain.User
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
    private val roleRepository: RoleRepository
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
        val user = userRepository.findByEmail(attributes.email)
            ?.apply {
                // 기존 사용자인 경우, 이름이 바뀌었으면 업데이트
                if (this.name != attributes.name) {
                    this.name = attributes.name
                }
            }
            ?: run {
                // 신규 사용자인 경우, 자동으로 생성
                val defaultRole = roleRepository.findByRoleName("ROLE_USER") ?: throw RoleNotFoundException()
                User(
                    email = attributes.email,
                    name = attributes.name,
                    password = null, // 소셜 로그인은 비밀번호 없음
                    birthday = null, // 추가 정보는 null로 시작
                    isMale = null, // 추가 정보는 null로 시작
                    loginType = attributes.registrationId.uppercase()
                ).apply {
                    this.roles.add(defaultRole)
                }
            }
        return userRepository.save(user)
    }
}

data class OAuth2Attributes(
    val attributes: Map<String, Any>,
    val nameAttributeKey: String,
    val name: String,
    val email: String,
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
            // profile 정보는 없을 수 있으므로 안전한 캐스팅 사용
            val profile = kakaoAccount["profile"] as? Map<String, Any>
            val nickname = profile?.get("nickname") as? String

            return OAuth2Attributes(
                attributes = attributes,
                nameAttributeKey = userNameAttributeName,
                name = nickname ?: "사용자", // 닉네임이 없으면 "사용자"를 기본값으로 사용
                email = kakaoAccount["email"] as String,
                registrationId = "kakao"
            )
        }
    }
}


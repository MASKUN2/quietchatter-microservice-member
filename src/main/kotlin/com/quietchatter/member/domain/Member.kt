package com.quietchatter.member.domain

import com.quietchatter.member.persistence.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table

@Entity
@Table(name = "member")
class Member(
    @Column(name = "nickname")
    var nickname: String,

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    var role: Role = Role.REGULAR,

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    var status: Status = Status.ACTIVE,

    @Column(name = "provider")
    @Enumerated(EnumType.STRING)
    var provider: OauthProvider = OauthProvider.NONE,

    @Column(name = "provider_id")
    var providerId: String? = null
) : BaseEntity() {

    companion object {
        fun newNaverMember(providerId: String, nickname: String): Member {
            return Member(
                nickname = nickname,
                role = Role.REGULAR,
                status = Status.ACTIVE,
                provider = OauthProvider.NAVER,
                providerId = providerId
            )
        }
    }

    fun activate() {
        this.status = Status.ACTIVE
    }

    fun deactivate() {
        this.status = Status.DEACTIVATED
    }

    fun updateNickname(nickname: String) {
        this.nickname = nickname
    }
}

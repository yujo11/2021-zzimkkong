package com.woowacourse.zzimkkong.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@NoArgsConstructor
@Entity
public class Preset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String name;

    @Embedded
    private Setting setting;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", foreignKey = @ForeignKey(name = "fk_preset_member"), nullable = false)
    private Member member;

    public Preset(final String name, final Setting setting, final Member member) {
        this.name = name;
        this.setting = setting;
        this.member = member;
        this.member.addPreset(this);
    }

    public Preset(final Long id, final String name, final Setting setting, final Member member) {
        this(name, setting, member);
        this.id = id;
    }

    public boolean isNotOwnedBy(final Member member) {
        return this.member.equals(member);
    }

    public boolean hasSameId(final Long presetId) {
        return this.id.equals(presetId);
    }
}

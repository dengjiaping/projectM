package com.yywl.projectT.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yywl.projectT.dmo.BadgeDetailsDmo;

public interface BadgeDetailsDao extends JpaRepository<BadgeDetailsDmo, Long> {

}

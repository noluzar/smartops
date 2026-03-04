package com.noluthando.smartops.repository;

import com.noluthando.smartops.entity.Project;
import com.noluthando.smartops.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByOwner(User owner);
    List<Project> findByMembersContaining(User user);
}
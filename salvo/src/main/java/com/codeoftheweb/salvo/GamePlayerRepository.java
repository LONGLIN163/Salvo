package com.codeoftheweb.salvo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

//@Entity class will be like a row in database,and Repository class will be like a table:row and table.
@RepositoryRestResource
public interface GamePlayerRepository extends JpaRepository<GamePlayer, Long>  {
}
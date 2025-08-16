package com.shop.respawn.repository;

import com.shop.respawn.domain.PointLedger;
import org.springframework.data.jpa.repository.*;

public interface PointLedgerRepository extends JpaRepository<PointLedger, Long>, PointLedgerRepositoryCustom {

}

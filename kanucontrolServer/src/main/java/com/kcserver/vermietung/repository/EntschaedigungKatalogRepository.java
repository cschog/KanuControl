package com.kcserver.vermietung.repository;
import com.kcserver.vermietung.entity.EntschaedigungKatalog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface EntschaedigungKatalogRepository extends JpaRepository<EntschaedigungKatalog,Long> {
 List<EntschaedigungKatalog> findByMietobjektIdOrderBySortierungAscIdAsc(Long id);
}

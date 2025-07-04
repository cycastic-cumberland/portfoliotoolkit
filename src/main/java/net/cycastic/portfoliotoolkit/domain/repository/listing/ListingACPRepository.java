package net.cycastic.portfoliotoolkit.domain.repository.listing;

import jakarta.validation.constraints.NotNull;
import net.cycastic.portfoliotoolkit.domain.model.ListingAccessControlPolicy;
import net.cycastic.portfoliotoolkit.domain.model.Project;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ListingACPRepository extends JpaRepository<ListingAccessControlPolicy, Integer> {
    void deleteByIdIsIn(@NotNull Collection<Integer> ids);

    List<ListingAccessControlPolicy> findListingAccessControlPoliciesByProject(@NotNull Project project);
    List<ListingAccessControlPolicy> findListingAccessControlPoliciesByProject(@NotNull Project project, @NotNull Sort sort);
}

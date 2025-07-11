package net.cycastic.portfoliotoolkit.application.listing.get;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import net.cycastic.portfoliotoolkit.application.listing.service.ListingService;
import net.cycastic.portfoliotoolkit.domain.dto.listing.ListingDto;
import net.cycastic.portfoliotoolkit.domain.exception.RequestException;
import net.cycastic.portfoliotoolkit.domain.repository.ProjectRepository;
import net.cycastic.portfoliotoolkit.domain.repository.listing.ListingRepository;
import net.cycastic.portfoliotoolkit.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class GetListingCommandHandler implements Command.Handler<GetListingCommand, ListingDto> {
    private final ListingService listingService;
    private final LoggedUserAccessor loggedUserAccessor;
    private final ListingRepository listingRepository;
    private final ProjectRepository projectRepository;

    private ListingDto handle(GetListingCommand command, @NotNull Integer projectId, boolean verifyAccess){
        var project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RequestException(404, "Project not found"));

        if (verifyAccess){
            listingService.verifyAccess(project, Stream.of(command.getListingPath()));
        }

        var listing = listingRepository.findByProjectAndListingPath(project, command.getListingPath())
                .orElseThrow(() -> new RequestException(404, "Listing not found"));
        return listingService.toDto(listing);
    }

    @Override
    public ListingDto handle(GetListingCommand command) {
        if (command.getProjectId() != null){
            return handle(command, command.getProjectId(), true);
        }

        return handle(command, loggedUserAccessor.getProjectId(), false);
    }
}

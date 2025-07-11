package net.cycastic.portfoliotoolkit.application.listing.attachment.replace;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.Null;
import lombok.Data;

@Data
public class OverwriteAttachmentListingCommand implements Command<@Null Object> {
    private String sourcePath;
    private String destinationPath;
}

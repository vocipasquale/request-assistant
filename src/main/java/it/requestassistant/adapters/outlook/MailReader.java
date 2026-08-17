package it.requestassistant.adapters.outlook;

import com.jacob.com.Dispatch;
import it.requestassistant.domain.model.Message;

import java.util.List;

public interface MailReader {

    List<Message> getMessages(String rootFolderName, String folderName);
    Message mailToMessage(Dispatch mail);

}

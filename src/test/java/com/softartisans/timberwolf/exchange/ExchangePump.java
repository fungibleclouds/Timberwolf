package com.softartisans.timberwolf.exchange;

import com.microsoft.schemas.exchange.services.x2006.messages.CreateFolderResponseType;
import com.microsoft.schemas.exchange.services.x2006.messages.CreateFolderType;
import com.microsoft.schemas.exchange.services.x2006.messages.CreateItemType;
import com.microsoft.schemas.exchange.services.x2006.messages.FolderInfoResponseMessageType;
import com.microsoft.schemas.exchange.services.x2006.types.BodyTypeType;
import com.microsoft.schemas.exchange.services.x2006.types.DistinguishedFolderIdNameType;
import com.microsoft.schemas.exchange.services.x2006.types.FolderType;
import com.microsoft.schemas.exchange.services.x2006.types.MessageDispositionType;
import com.microsoft.schemas.exchange.services.x2006.types.MessageType;
import com.microsoft.schemas.exchange.services.x2006.types.NonEmptyArrayOfAllItemsType;
import com.microsoft.schemas.exchange.services.x2006.types.NonEmptyArrayOfFoldersType;
import com.microsoft.schemas.exchange.services.x2006.types.TargetFolderIdType;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import org.xmlsoap.schemas.soap.envelope.BodyType;
import org.xmlsoap.schemas.soap.envelope.EnvelopeDocument;
import org.xmlsoap.schemas.soap.envelope.EnvelopeType;

/**
 * This class puts a bunch of folders or emails into an account in exchange.
 * This should only be used by integration tests.
 */
public class ExchangePump
{
    private static final String DECLARATION = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
    private static final String SOAP_ENCODING = "UTF-8";

    private String endpoint;
    private HttpUrlConnectionFactory connectionFactory = new AlfredoHttpUrlConnectionFactory();
    private String sender;

    public ExchangePump(String exchangeUrl, String senderEmail)
    {
        endpoint = exchangeUrl;
        sender = senderEmail;
    }

    private EnvelopeDocument createEmptyRequest(String user)
    {
        EnvelopeDocument request = EnvelopeDocument.Factory.newInstance();
        EnvelopeType envelope = request.addNewEnvelope();
        envelope.addNewHeader().addNewExchangeImpersonation().addNewConnectingSID().setPrincipalName(user);
        return request;
    }

    private List<String> createFolders(String user, TargetFolderIdType parentFolderId, List<RequiredFolder> folders)
    {
        EnvelopeDocument request = createEmptyRequest(user);
        CreateFolderType createFolder = request.getEnvelope().addNewBody().addNewCreateFolder();
        createFolder.setParentFolderId(parentFolderId);
        NonEmptyArrayOfFoldersType requestedFolders = createFolder.addNewFolders();
        for (RequiredFolder folder : folders)
        {
            requestedFolders.addNewFolder().setDisplayName(folder.getName());
        }
        CreateFolderResponseType response = sendRequest(request).getCreateFolderResponse();
        FolderInfoResponseMessageType[] array = response.getResponseMessages().getCreateFolderResponseMessageArray();
        List<String> folderIds = new ArrayList<String>();
        int i=0;
        for (FolderInfoResponseMessageType folderResponse : array)
        {
            for (FolderType folder : folderResponse.getFolders().getFolderArray())
            {
                folders.get(i).setId(folder.getFolderId().getId());
                i++;
            }
        }
        return folderIds;
    }

    public List<String> createFolders(String user, String parent, List<RequiredFolder> folders)
    {
        TargetFolderIdType parentFolder = TargetFolderIdType.Factory.newInstance();
        parentFolder.addNewFolderId().setId(parent);
        return createFolders(user, parentFolder, folders);
    }

    public List<String> createFolders(String user, DistinguishedFolderIdNameType.Enum parent, List<RequiredFolder> folders)
    {
        TargetFolderIdType parentFolder = TargetFolderIdType.Factory.newInstance();
        parentFolder.addNewDistinguishedFolderId().setId(parent);
        return createFolders(user, parentFolder, folders);
    }

    public void sendMessages(Message... messages)
    {
        EnvelopeDocument request = createEmptyRequest(sender);
        CreateItemType createItem = request.getEnvelope().addNewBody().addNewCreateItem();
        createItem.setMessageDisposition(MessageDispositionType.SEND_ONLY);
        NonEmptyArrayOfAllItemsType items = createItem.addNewItems();

        // TODO actually use the message, may merge with EmailMatcher
        MessageType exchangeMessage = items.addNewMessage();
        exchangeMessage.addNewFrom().addNewMailbox().setEmailAddress("tsender@int.tartarus.com");
        com.microsoft.schemas.exchange.services.x2006.types.BodyType body = exchangeMessage.addNewBody();
        body.setBodyType(BodyTypeType.TEXT);
        body.setStringValue("The body of the email");
        exchangeMessage.setSubject("The subject of the email");
        exchangeMessage.addNewCcRecipients().addNewMailbox().setEmailAddress("ccboo@int.tartarus.com");
        exchangeMessage.addNewBccRecipients().addNewMailbox().setEmailAddress("bccboo@int.tartarus.com");

        BodyType response = sendRequest(request);
        System.err.println(response);
        if (response == null)
        {
            System.err.println("Got null response when creating messages");
        }
    }

    private BodyType sendRequest(final EnvelopeDocument envelope)
    {
        String request = DECLARATION + envelope.xmlText();
        try
        {
            HttpURLConnection conn = connectionFactory.newInstance(endpoint, request.getBytes(SOAP_ENCODING));

            int code = conn.getResponseCode();

            InputStream responseData = conn.getInputStream();

            int amtAvailable = responseData.available();

            if (code == HttpURLConnection.HTTP_OK)
            {
                EnvelopeDocument response = EnvelopeDocument.Factory.parse(responseData);
                return response.getEnvelope().getBody();
            }
            else
            {
                System.err.println("HTTP_ERROR: " + code);
                return null;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }

    }

    /** A simple class representing an email */
    public class Message
    {

        private String from;

        public String getFrom()
        {
            return from;
        }
    }
}

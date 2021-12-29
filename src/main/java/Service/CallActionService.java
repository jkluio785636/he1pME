package Service;

import SpecialDataStructure.CallAction;
import SpecialDataStructure.UrlType;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class CallActionService extends CommonService {

    protected void callAction(final MessageCreateEvent event, final String instruction) {
        final MessageChannel messageChannel = Objects.requireNonNull(event.getMessage().getChannel().block());
        final CallAction callAction = getCallAction(instruction);
        messageChannel.createMessage("<@" + callAction.getId() + "> " + callAction.getMessage()).block();
        messageChannel.createMessage(
                EmbedCreateSpec.create().withColor(callAction.getColor()).withImage(callAction.getImage())
        ).block();
    }

    private CallAction getCallAction(final String searchValue) {
        final Map<String, String> nameMap = Collections.singletonMap("#key", "action");
        final Map<String, Object> valueMap = Collections.singletonMap(":value", searchValue);
        final QuerySpec querySpec = new QuerySpec().withKeyConditionExpression("#key = :value")
                .withNameMap(nameMap).withValueMap(valueMap);
        final ItemCollection<QueryOutcome> items = dynamoDB.getTable("CallAction").query(querySpec);
        final CallAction result = new CallAction();
        if (items.iterator().hasNext()) {
            final Item item = items.iterator().next();
            result.setId(getIdFromDB(item.getString("name")));
            result.setMessage(item.getString("message"));
            result.setImage(getUrlFromDB(item.getString("image"), UrlType.IMAGE));
            final String[] color = item.getString("color").split(", ");
            result.setColor(Color.of(Integer.parseInt(color[0]), Integer.parseInt(color[1]), Integer.parseInt(color[2])));
        }
        return result;
    }
}
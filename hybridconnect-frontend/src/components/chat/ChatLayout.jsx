import ConversationList from "./ConversationList";
import ChatHeader from "./ChatHeader";
import MessageList from "./MessageList";
import Composer from "./Composer";

export default function ChatLayout(props) {
  return (
    <div className="appRoot">
      <div id="left">
        <ConversationList {...props} />
      </div>

      <div id="right">
        <ChatHeader {...props} />
        <MessageList {...props} />
        <Composer {...props} />
      </div>
    </div>
  );
}

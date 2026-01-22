function Chat() {
  return (
    <div style={{ display: "flex", height: "100vh" }}>
      
      {/* Left */}
      <div style={{ width: 300, borderRight: "1px solid #ddd", padding: 10 }}>
        <h3>Chats</h3>
        <div>👤 Nimali</div>
        <div>👤 Kamal</div>
      </div>

      {/* Right */}
      <div style={{ flex: 1, padding: 10 }}>
        <h3>Chat window</h3>

        <div style={{ height: "80%", border: "1px solid #ddd", marginBottom: 10 }}>
          Messages here...
        </div>

        <input placeholder="Type message..." style={{ width: "80%" }} />
        <button>Send</button>
      </div>

    </div>
  )
}

export default Chat

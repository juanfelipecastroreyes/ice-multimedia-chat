#pragma once

module ChatApp
{
    const int FileChunkSize = 65536;
    const int AudioSampleRate = 16000;
    const int AudioFrameBytes = 640;

    sequence<byte> Bytes;
    sequence<string> StringSeq;

    exception ChatException
    {
        string reason;
    };

    exception NotLoggedIn extends ChatException {};
    exception NicknameInUse extends ChatException {};
    exception UserNotFound extends ChatException {};
    exception RoomNotFound extends ChatException {};
    exception RoomAlreadyExists extends ChatException {};
    exception NotInRoom extends ChatException {};
    exception CallNotFound extends ChatException {};
    exception TransferNotFound extends ChatException {};

    struct Message
    {
        string sender;
        string recipient;
        string room;
        string text;
        long timestamp;
    };

    struct FileInfo
    {
        string transferId;
        string sender;
        string recipient;
        string room;
        string fileName;
        long size;
        int totalChunks;
        string sha256;
    };

    struct CallInfo
    {
        int callId;
        string caller;
        string room;
        bool group;
    };

    struct MediaTicket
    {
        int callId;
        int token;
        string relayHost;
        int relayPort;
    };

    interface ClientCallback
    {
        void onMessage(Message msg);
        void onUserOnline(string nickname);
        void onUserOffline(string nickname);
        void onRoomJoined(string room, string nickname);
        void onRoomLeft(string room, string nickname);

        void onFileStart(FileInfo info);
        void onFileChunk(string transferId, int index, Bytes data);
        void onFileEnd(string transferId);

        void onIncomingCall(CallInfo call);
        void onCallAccepted(int callId, string nickname);
        void onCallRejected(int callId, string nickname);
        void onCallLeft(int callId, string nickname);
        void onCallEnded(int callId);
    };

    interface ChatServer
    {
        void login(string nickname, ClientCallback* callback) throws NicknameInUse;
        void logout();
        StringSeq listUsers() throws NotLoggedIn;

        void sendPrivate(string recipient, string text) throws NotLoggedIn, UserNotFound;

        void createRoom(string room) throws NotLoggedIn, RoomAlreadyExists;
        StringSeq listRooms() throws NotLoggedIn;
        void joinRoom(string room) throws NotLoggedIn, RoomNotFound;
        void leaveRoom(string room) throws NotLoggedIn, RoomNotFound, NotInRoom;
        StringSeq listRoomMembers(string room) throws NotLoggedIn, RoomNotFound;
        void sendToRoom(string room, string text) throws NotLoggedIn, RoomNotFound, NotInRoom;

        string startFile(FileInfo info) throws NotLoggedIn, UserNotFound, RoomNotFound, NotInRoom;
        void sendChunk(string transferId, int index, Bytes data) throws NotLoggedIn, TransferNotFound;
        void endFile(string transferId) throws NotLoggedIn, TransferNotFound;

        MediaTicket startCall(string recipient) throws NotLoggedIn, UserNotFound;
        MediaTicket startRoomCall(string room) throws NotLoggedIn, RoomNotFound, NotInRoom;
        MediaTicket acceptCall(int callId) throws NotLoggedIn, CallNotFound;
        void rejectCall(int callId) throws NotLoggedIn, CallNotFound;
        void hangup(int callId) throws NotLoggedIn, CallNotFound;
    };
};

package com.example.rayzi.socket;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.example.rayzi.BuildConfig;
import com.example.rayzi.SessionManager;
import com.example.rayzi.retrofit.Const;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.engineio.client.transports.WebSocket;

public class MySocketManager {

    private static final String TAG = "SocketManager_1";
    public static int lisenerCount = 0;
    private final List<LiveHandler> liveHandlers = new ArrayList<>();
    private final List<ChatHandler> chatHandlers = new ArrayList<>();
    private final List<CallHandler> callHandlers = new ArrayList<>();
    List<AudioRoomHandler> audioRoomHandlerList = new ArrayList<>();
    public boolean lastCallCancalled = false;
    public boolean globalConnecting = false;
    public boolean globalConnected = false;
    List<SocketConnectHandler> socketConnectHandlerList = new ArrayList<>();
    Handler handler = new Handler();
    SessionManager sessionManager;
    String userId;
    Socket socket;
    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (socket != null) {
                Log.d(TAG, "run: SOCKET CONNECTE = " + socket.connected());
            }
            handler.postDelayed(this, 3000);
        }
    };

    public MySocketManager() {

    }

    public static MySocketManager getInstance() {
        return Holder.INSTANCE;
    }

    public void createGlobal(Context applicationContext) {

        if (MySocketManager.getInstance().getSocet() != null) {
            if (MySocketManager.getInstance().getSocet().connected()) {
                return;
            }
        }

        Log.d("{{{{{{{{{{{{{{{{{{{{{TAG}}}}}}}}}}}}}}}}}}}}}", "createGlobal: ");

        sessionManager = new SessionManager(applicationContext);

        if (sessionManager.getUser() == null) {
            Log.d(TAG, "createGlobal: not Logged yet");
            return;
        }

        userId = sessionManager.getUser().getId();

        Log.d(TAG, "initGlobalSocket: init " + userId);
        IO.Options options = IO.Options.builder()
                // IO factory options
                .setForceNew(false)
                .setMultiplex(true)
                // low-level engine options
                .setTransports(new String[]{WebSocket.NAME})
                .setUpgrade(false)
                .setRememberUpgrade(false)
                .setPath("/socket.io/")
                .setQuery("globalRoom=" + "globalRoom:" + userId + "")
                .setExtraHeaders(null)
                // Manager options
                .setReconnection(true)
                .setReconnectionAttempts(Integer.MAX_VALUE)
                .setReconnectionDelay(1000)
                .setReconnectionDelayMax(5000)
                .setRandomizationFactor(0.5)
//                .setTimeout(3000)
                // Socket options
                .setAuth(null)
                .build();

        URI uri = URI.create(BuildConfig.BASE_URL);
        socket = IO.socket(uri, options);
        socket.connect();

        Log.d(TAG, "createGlobal: SSS97  " + socket.connected());

        socket.io().on("reconnect", args1 -> {
            for (SocketConnectHandler connectHandler : socketConnectHandlerList) {
                connectHandler.onReconnected(args1);
            }
            Log.d(TAG, "reconnected: 111   listner count>> " + lisenerCount);

        });
        socket.io().on("reconnection_attempt", args -> {
            Log.d(TAG, "reconnection_attempt :111 ");
            for (SocketConnectHandler connectHandler : socketConnectHandlerList) {
                connectHandler.onReconnecting();
            }
        });
        socket.io().on("reconnected", args1 -> {
            for (SocketConnectHandler connectHandler : socketConnectHandlerList) {
                connectHandler.onReconnected(args1);
            }
            Log.d(TAG, "reconnected: 1111  listner count>> " + lisenerCount);
        });
        socket.once(Socket.EVENT_CONNECT, args -> {
            Log.d(TAG, "connected: globelSoket");
            globalConnected = true;
            lastCallCancalled = false;

            for (SocketConnectHandler connectHandler : socketConnectHandlerList) {
                connectHandler.onConnect();
                Log.d(TAG, "createGlobal: onconnect");
            }

            socket.io().on("reconnect", args1 -> {
                Log.d(TAG, "reconnect: 222   ");
            });

            socket.io().on("reconnected", args1 -> {
                Log.d(TAG, "reconnected: 222  listner count>> " + lisenerCount);
            });

            socket.io().on("reconnection_attempt", args1 -> {
                Log.d(TAG, "reconnection_attempt:222 ");
            });

            socket.on(Const.EVENT_SIMPLEFILTER, args1 -> {
                Log.d(TAG, "createGlobal: onsimplerfilter " + args1[0].toString());
                for (LiveHandler liveHandler : liveHandlers) {
                    liveHandler.onSimpleFilter(args1);
                }
            });

            socket.on(Const.EVENT_ANIMFILTER, args1 -> {
                Log.d(TAG, "createGlobal: onAnimateFilter " + args1[0].toString());
                for (LiveHandler liveHandler : liveHandlers) {
                    liveHandler.onAnimatedFilter(args1);
                }
            });

            socket.on(Const.EVENT_GIF, args1 -> {
                Log.d(TAG, "createGlobal: onGif " + args1[0].toString());
                for (LiveHandler liveHandler : liveHandlers) {
                    liveHandler.onGif(args1);
                }
            });

            socket.on(Const.EVENT_COMMENT, args1 -> {
                Log.d(TAG, "createGlobal: onComment " + args1[0].toString());
                for (LiveHandler liveHandler : liveHandlers) {
                    liveHandler.onComment(args1);
                }
            });

            socket.on(Const.EVENT_GIFT, args1 -> {
                Log.d(TAG, "createGlobal: onnGift " + args1[0].toString());
                for (LiveHandler liveHandler : liveHandlers) {
                    liveHandler.onGift(args1);
                }
            });

            socket.on(Const.EVENT_VIEW, args1 -> {
                Log.d(TAG, "createGlobal: onView " + args1[0].toString());
                for (LiveHandler liveHandler : liveHandlers) {
                    liveHandler.onView(args1);
                }
            });

//            socket.on(Const.EVENT_GET_USER, args1 -> {
//                Log.d(TAG, "createGlobal: onGetUser " + args1[0].toString());
//                for (LiveHandler liveHandler : liveHandlers) {
//                    liveHandler.onGetUser(args1);
//                }
//            });

            socket.on(Const.EVENT_BLOCK, args1 -> {
                Log.d(TAG, "createGlobal: onBlock " + args1[0].toString());
                for (LiveHandler liveHandler : liveHandlers) {
                    liveHandler.onBlock(args1);
                }
            });

            socket.on(Const.LIVE_REJOIN, args1 -> {
                Log.d(TAG, "createGlobal: onLiveREjoin: " + args1[0].toString());
                for (LiveHandler liveHandler : liveHandlers) {
                    liveHandler.onLiveRejoin(args1);
                }
            });

            socket.on(Const.EVENT_CHAT, args1 -> {
                Log.d(TAG, "createGlobal: onChat " + args1[0].toString());
                for (ChatHandler chatHandler : chatHandlers) {
                    chatHandler.onChat(args1);
                }
            });

            socket.on(Const.EVENT_CALL_REQUEST, args1 -> {
                Log.d(TAG, "createGlobal: onCallRequest  " + args1[0].toString());
                for (CallHandler callHandler : callHandlers) {
                    callHandler.onCallRequest(args1);
                }
            });

            socket.on(Const.EVENT_CALL_ANSWER, args1 -> {
                Log.d(TAG, "createGlobal: onCallAnswer  " + args1[0].toString());
                for (CallHandler callHandler : callHandlers) {
                    callHandler.onCallAnswer(args1);
                }
            });

            socket.on(Const.EVENT_CALL_RECIVE, args1 -> {
                Log.d(TAG, "createGlobal: onCallRecive " + args1[0].toString());
                for (CallHandler callHandler : callHandlers) {
                    callHandler.onCallReceive(args1);
                }
            });

            socket.on(Const.EVENT_CALL_CONFIRMED, args1 -> {
                Log.d(TAG, "createGlobal: onCallComfirm: " + args1[0].toString());
                for (CallHandler callHandler : callHandlers) {
                    callHandler.onCallConfirm(args1);
                }
            });

            socket.on(Const.EVENT_CALL_CANCEL, args1 -> {
                Log.d(TAG, "createGlobal: onCallCancel: " + args1[0].toString());
                for (CallHandler callHandler : callHandlers) {
                    callHandler.onCallCancel(args1);
                }
            });

            socket.on(Const.EVENT_CALL_DISCONNECT, args1 -> {
                Log.d(TAG, "createGlobal: onCallDisconnect: " + args1[0].toString());
                for (CallHandler callHandler : callHandlers) {
                    callHandler.onCallDisconnect(args1);
                }
            });

            socket.on(Const.EVENT_COMMENT, args1 -> {
                Log.d(TAG, "createGlobal: event comment " + args1[0].toString());
                for (AudioRoomHandler audioRoomHandler : audioRoomHandlerList) {
                    audioRoomHandler.onComment(args1);
                }
            });

            socket.on(Const.EVENT_GIFT, args1 -> {
                for (AudioRoomHandler audioRoomHandler : audioRoomHandlerList) {
                    audioRoomHandler.onGift(args1);
                }
                Log.d(TAG, "createGlobal: event gift " + args1[0].toString());
            });

            socket.on(Const.EVENT_VIEW, args1 -> {
                Log.d(TAG, "createGlobal: event view " + args1[0].toString());
                for (AudioRoomHandler audioRoomHandler : audioRoomHandlerList) {
                    audioRoomHandler.onView(args1);
                }
            });


            socket.on(Const.EVENT_BLOCK, args1 -> {
                Log.d(TAG, "createGlobal: event block " + args1[0].toString());
                for (AudioRoomHandler audioRoomHandler : audioRoomHandlerList) {
                    audioRoomHandler.onBlock(args1);
                }
            });

            socket.on(Const.EVENT_ADD_REQUESTED, args1 -> {
                Log.d(TAG, "createGlobal: EVENT_ADD_REQUESTED");
                for (AudioRoomHandler audioRoomHandler : audioRoomHandlerList) {
                    audioRoomHandler.onAddRequested(args1);
                }
            });
            socket.on(Const.EVENT_DECLINEiNVITE, args1 -> {
                Log.d(TAG, "createGlobal: EVENT_DECLINEiNVITE");
                for (AudioRoomHandler audioRoomHandler : audioRoomHandlerList) {
                    audioRoomHandler.onDeclineInvite(args1);
                }
            });
            socket.on(Const.EVENT_ADD_PARTICIPATED, args1 -> {
                Log.d(TAG, "createGlobal: EVENT_ADD_PARTICIPATED");
                for (AudioRoomHandler audioRoomHandler : audioRoomHandlerList) {
                    audioRoomHandler.onAddParticipants(args1);
                }
            });
            socket.on(Const.EVENT_LESS_PARTICIPATED, args1 -> {
                Log.d(TAG, "createGlobal: EVENT_LESS_PARTICIPATED");
                for (AudioRoomHandler audioRoomHandler : audioRoomHandlerList) {
                    audioRoomHandler.onLessParticipants(args1);
                }
            });
            socket.on(Const.EVENT_MUTESEAT, args1 -> {
                Log.d(TAG, "createGlobal: EVENT_MUTESEAT");
                for (AudioRoomHandler audioRoomHandler : audioRoomHandlerList) {
                    audioRoomHandler.onMuteSeat(args1);
                }
            });
            socket.on(Const.EVENT_LOCK_SEAT, args1 -> {
                Log.d(TAG, "createGlobal: EVENT_LOCK_SEAT");
                for (AudioRoomHandler audioRoomHandler : audioRoomHandlerList) {
                    audioRoomHandler.onLockSeat(args1);
                }
            });
            socket.on(Const.EVENT_ALL_SEAT_LOCK, args1 -> {
                Log.d(TAG, "createGlobal: EVENT_ALL_SEAT_LOCK");
                for (AudioRoomHandler audioRoomHandler : audioRoomHandlerList) {
                    audioRoomHandler.onAllSeatLock(args1);
                }
            });
            socket.on(Const.EVENT_CHANGE_THEME, args1 -> {
                Log.d(TAG, "createGlobal: EVENT_CHANGE_THEME");
                for (AudioRoomHandler audioRoomHandler : audioRoomHandlerList) {
                    audioRoomHandler.onChangeTheme(args1);
                }
            });
            socket.on(Const.EVENT_SEAT, args1 -> {
                Log.d(TAG, "createGlobal: EVENT_SEAT seat seat seat listen thai che ");
                for (AudioRoomHandler audioRoomHandler : audioRoomHandlerList) {
                    audioRoomHandler.onSeat(args1);
                }
            });
            socket.on(Const.EVENT_GET_USER, args1 -> {
                Log.d(TAG, "createGlobal: EVENT_SEAT get user");
                for (AudioRoomHandler audioRoomHandler : audioRoomHandlerList) {
                    audioRoomHandler.onGetUser(args1);
                }
            });

            socket.on("data", args1 -> {
                Log.d(TAG, "createGlobal: dummy listener");
                for (AudioRoomHandler audioRoomHandler : audioRoomHandlerList) {
                    audioRoomHandler.onGetUser(args1);
                }
            });
            socket.on("liveHostEnd", args1 -> {
                Log.d(TAG, "createGlobal: liveEnd listener");
                for (AudioRoomHandler audioRoomHandler : audioRoomHandlerList) {
                    audioRoomHandler.onLiveEnd(args1);
                }
            });

            socket.on(Const.EVENT_INVITE, args1 -> {
                Log.d(TAG, "createGlobal: EVENT_SEAT");
                for (AudioRoomHandler audioRoomHandler : audioRoomHandlerList) {
                    audioRoomHandler.onInvite(args1);
                }
            });

            handler.removeCallbacksAndMessages(null);
            handler.postDelayed(timerRunnable, 3000);
        });

        socket.on(Const.EVENTSENDREACTION, args1 -> {
            Log.d(TAG, "createGlobal: roomImage  " + args1[0].toString());
            for (AudioRoomHandler audioRoomHandler : audioRoomHandlerList) {
                audioRoomHandler.onReactionReceived(args1);
            }
            for (LiveHandler liveHandler : liveHandlers) {
                liveHandler.onReactionRecived(args1);
            }
        });

        socket.on(Const.EVENT_ROOMNAME,args1 -> {
            Log.d(TAG, "createGlobal: EVENT_ROOMNAME  " + args1[0].toString());
            for (AudioRoomHandler audioRoomHandler : audioRoomHandlerList) {
                audioRoomHandler.onRoomNameChange(args1);
            }
        });

        socket.on(Const.EVENT_ROOMWELCOME,args1 -> {
            Log.d(TAG, "createGlobal: EVENT_ROOMWELCOME  " + args1[0].toString());
            for (AudioRoomHandler audioRoomHandler : audioRoomHandlerList) {
                audioRoomHandler.onWelcomeMessage(args1);
            }
        });

        socket.on("roomImage",args1 -> {
            Log.d(TAG, "createGlobal: EVENT_ROOMWELCOME  " + args1[0].toString());
            for (AudioRoomHandler audioRoomHandler : audioRoomHandlerList) {
                audioRoomHandler.onRoomImageChange(args1);
            }
        });

    }

    public Socket getSocket() {
        return socket;
    }

    public void addSocketConnectHandler(SocketConnectHandler socketConnectHandler) {
        socketConnectHandlerList.add(socketConnectHandler);
    }

    public void removeSocketConnectHandler(SocketConnectHandler socketConnectHandler) {
        socketConnectHandlerList.remove(socketConnectHandler);
    }


    public void addLiveListener(LiveHandler liveHandler) {
        liveHandlers.add(liveHandler);
    }

    public void removeLiveListener(LiveHandler liveHandler) {
        liveHandlers.remove(liveHandler);
    }

    public void addChatListener(ChatHandler chatListener) {
        chatHandlers.add(chatListener);
    }

    public void removeChatListener(ChatHandler chatListener) {
        chatHandlers.remove(chatListener);
    }

    public void addCallListener(CallHandler callHandler) {
        callHandlers.add(callHandler);
    }

    public void removeCallListener(CallHandler callHandler) {
        callHandlers.remove(callHandler);
    }

    public Socket getSocet() {
        return socket;
    }

    public void addAudioRoomHandler(AudioRoomHandler audioRoomHandler) {
        audioRoomHandlerList.add(audioRoomHandler);
    }

    public void removeAudioRoomHandler(AudioRoomHandler audioRoomHandler) {
        audioRoomHandlerList.remove(audioRoomHandler);
    }

    private static final class Holder {
        private static final MySocketManager INSTANCE = new MySocketManager();
    }

}

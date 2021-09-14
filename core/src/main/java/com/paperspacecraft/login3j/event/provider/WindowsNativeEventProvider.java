package com.paperspacecraft.login3j.event.provider;

import com.paperspacecraft.login3j.event.NativeEventProvider;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class WindowsNativeEventProvider extends NativeEventProvider {
    private static final Logger LOG = LoggerFactory.getLogger(WindowsNativeEventProvider.class);

    private static final int COMPLETION_TIMEOUT = 1000;
    private static final String INTERRUPTED_MESSAGE = "Thread {} interrupted";

    private final WinDef.HINSTANCE moduleHandle;
    private final AtomicReference<KeyboardProc> keyboardProcReference = new AtomicReference<>();

    private volatile int keyboardHookThreadId;
    private final CountDownLatch keyboardHookSetFlag = new CountDownLatch(1);
    private final CountDownLatch keyboardHookUnsetFlag = new CountDownLatch(1);

    private volatile int mouseHookThreadId;
    private final CountDownLatch mouseHookSetFlag = new CountDownLatch(1);
    private final CountDownLatch mouseHookUnsetFlag = new CountDownLatch(1);


    public WindowsNativeEventProvider() {
         moduleHandle = Kernel32.INSTANCE.GetModuleHandle(null);
    }

    @Override
    public synchronized boolean enable() {
        return enable(new KeyboardHookTask(), keyboardHookThreadId, keyboardHookSetFlag)
                && enable(new MouseHookTask(), mouseHookThreadId, mouseHookSetFlag);
    }

    private boolean enable(Runnable task, int threadId, CountDownLatch setFlag) {
        if (threadId != 0) {
            return true;
        }
        Thread hookThread = new Thread(task);
        hookThread.start();
        return waitCompleted(setFlag);
    }

    @Override
    public synchronized boolean disable() {
        return disable(keyboardHookThreadId, keyboardHookUnsetFlag)
                && disable(mouseHookThreadId, mouseHookUnsetFlag);
    }

    private boolean disable(int threadId, CountDownLatch unsetFlag) {
        if (threadId == 0) {
            return true;
        }
        User32.INSTANCE.PostThreadMessage(threadId, WinUser.WM_QUIT, new WinDef.WPARAM(), new WinDef.LPARAM());
        return waitCompleted(unsetFlag);
    }

    private boolean waitCompleted(CountDownLatch flag) {
        try {
            return flag.await(COMPLETION_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            LOG.warn(INTERRUPTED_MESSAGE, Thread.currentThread().getId());
            Thread.currentThread().interrupt();
        }
        return false;
    }

    private class KeyboardHookTask implements Runnable {

        @Override
        public void run() {
            keyboardHookThreadId = Kernel32.INSTANCE.GetCurrentThreadId();
            AtomicReference<WinUser.HHOOK> hookReference = new AtomicReference<>();

            KeyboardProc keyboardProc = new KeyboardProc();
            keyboardProc.setHookReference(hookReference);
            keyboardProc.setKeyTypedCallback(getKeyTypedCallback());

            WinUser.HHOOK hook = User32.INSTANCE.SetWindowsHookEx(WinUser.WH_KEYBOARD_LL, keyboardProc, moduleHandle, 0);
            if (hook != null) {
                LOG.info("Keyboard hook installed");
            } else {
                LOG.error("Could not install keyboard hook");
                return;
            }
            hookReference.set(hook);
            keyboardProcReference.set(keyboardProc);
            keyboardHookSetFlag.countDown();

            User32.INSTANCE.GetMessage(new WinUser.MSG(), null, 0, 0);

            User32.INSTANCE.UnhookWindowsHookEx(hook);
            keyboardHookThreadId = 0;
            keyboardHookUnsetFlag.countDown();
            LOG.info("Keyboard hook removed");
        }
    }

    private class MouseHookTask implements Runnable {

        @Override
        public void run() {
            mouseHookThreadId = Kernel32.INSTANCE.GetCurrentThreadId();
            AtomicReference<WinUser.HHOOK> hookReference = new AtomicReference<>();

            MouseProc mouseProc = new MouseProc();
            mouseProc.setKeyboardProc(keyboardProcReference.get());
            mouseProc.setMouseDownCallback(getMouseDownCallback());
            mouseProc.setMouseClickCallback(getMouseClickCallback());

            WinUser.HHOOK hook = User32.INSTANCE.SetWindowsHookEx(WinUser.WH_MOUSE_LL, mouseProc, moduleHandle, 0);
            if (hook != null) {
                LOG.info("Mouse hook installed");
            } else {
                LOG.error("Could not install mouse hook");
                return;
            }
            hookReference.set(hook);
            mouseHookSetFlag.countDown();

            User32.INSTANCE.GetMessage(new WinUser.MSG(), null, 0, 0);

            User32.INSTANCE.UnhookWindowsHookEx(hook);
            mouseHookThreadId = 0;
            mouseHookUnsetFlag.countDown();
            LOG.info("Mouse hook removed");
        }
    }
}

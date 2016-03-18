#include <jvmti.h>
#include <stdio.h>
#include <string.h>

const char* pattern = "";

static void printMethod(jvmtiEnv* jvmti, jmethodID method, const char* prefix) {
  char *name, *sig, *cl;
  jclass javaClass;
  jvmti->GetMethodDeclaringClass(method, &javaClass);
  jvmti->GetClassSignature(javaClass, &cl, NULL);
  ++cl; // Ignore leading 'L'
  if (strstr(cl, pattern) == cl) {
    jvmti->GetMethodName(method, &name, &sig, NULL);
    cl[strlen(cl) - 1] = '\0'; // Strip trailing ';'
    fprintf(stdout, "%s %s::%s%s\n", prefix, cl, name, sig);
    fflush (NULL);
    jvmti->Deallocate((unsigned char*) name);
    jvmti->Deallocate((unsigned char*) sig);
  }
  jvmti->Deallocate((unsigned char*) --cl);
}

void JNICALL methodEntryCallback(jvmtiEnv* jvmti, JNIEnv* jni, jthread thread, jmethodID method) {
  printMethod(jvmti, method, "->");
}

void JNICALL methodExitCallback(jvmtiEnv* jvmti, JNIEnv* jni, jthread thread, jmethodID method, jboolean except, jvalue ret_val) {
  printMethod(jvmti, method, "<-");
}

extern "C"
JNIEXPORT jint JNICALL Agent_OnLoad(JavaVM* jvm, char* options, void* reserved) {
  jvmtiEnv* jvmti = NULL;
  jvmtiCapabilities capa;
  jvmtiError error;

  if (options) pattern = strdup(options); // Options may contain the pattern

  jint result = jvm->GetEnv((void**) &jvmti, JVMTI_VERSION_1_1);
  if (result != JNI_OK) {
    fprintf(stderr, "Can't access JVMTI!\n");
    return JNI_ERR;
  }

  memset(&capa, 0, sizeof(jvmtiCapabilities));
  capa.can_generate_method_entry_events = 1;
  capa.can_generate_method_exit_events = 1;
  if (jvmti->AddCapabilities(&capa) != JVMTI_ERROR_NONE) {
    fprintf(stderr, "Can't set capabilities!\n");
    return JNI_ERR;
  }
  jvmtiEventCallbacks callbacks;
  memset(&callbacks, 0, sizeof(jvmtiEventCallbacks));
  callbacks.MethodEntry = methodEntryCallback;
  callbacks.MethodExit = methodExitCallback;
  if (jvmti->SetEventCallbacks(&callbacks, sizeof(jvmtiEventCallbacks)) != JVMTI_ERROR_NONE) {
    fprintf(stderr, "Can't set event callbacks!\n");
    return JNI_ERR;
  }
  if (jvmti->SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_METHOD_ENTRY, NULL) != JVMTI_ERROR_NONE) {
    fprintf(stderr, "Can't enable JVMTI_EVENT_METHOD_ENTRY!\n");
    return JNI_ERR;
  }
  if (jvmti->SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_METHOD_EXIT, NULL) != JVMTI_ERROR_NONE) {
    fprintf(stderr, "Can't enable JVMTI_EVENT_METHOD_EXIT!\n");
    return JNI_ERR;
  }
}

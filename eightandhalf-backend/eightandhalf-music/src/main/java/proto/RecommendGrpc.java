package proto;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 *ip: 43.138.152.89:9090
 * The service definition.
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.68.1)",
    comments = "Source: recommend.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class RecommendGrpc {

  private RecommendGrpc() {}

  public static final String SERVICE_NAME = "proto.Recommend";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<RecommendOuterClass.RecommendRequest,
      RecommendOuterClass.RecommendReply> getGetMusicMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetMusic",
      requestType = RecommendOuterClass.RecommendRequest.class,
      responseType = RecommendOuterClass.RecommendReply.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<RecommendOuterClass.RecommendRequest,
      RecommendOuterClass.RecommendReply> getGetMusicMethod() {
    io.grpc.MethodDescriptor<RecommendOuterClass.RecommendRequest, RecommendOuterClass.RecommendReply> getGetMusicMethod;
    if ((getGetMusicMethod = RecommendGrpc.getGetMusicMethod) == null) {
      synchronized (RecommendGrpc.class) {
        if ((getGetMusicMethod = RecommendGrpc.getGetMusicMethod) == null) {
          RecommendGrpc.getGetMusicMethod = getGetMusicMethod =
              io.grpc.MethodDescriptor.<RecommendOuterClass.RecommendRequest, RecommendOuterClass.RecommendReply>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetMusic"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  RecommendOuterClass.RecommendRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  RecommendOuterClass.RecommendReply.getDefaultInstance()))
              .setSchemaDescriptor(new RecommendMethodDescriptorSupplier("GetMusic"))
              .build();
        }
      }
    }
    return getGetMusicMethod;
  }

  private static volatile io.grpc.MethodDescriptor<RecommendOuterClass.UploadMusicRequest,
      RecommendOuterClass.UploadMusicReply> getUploadMusicMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "UploadMusic",
      requestType = RecommendOuterClass.UploadMusicRequest.class,
      responseType = RecommendOuterClass.UploadMusicReply.class,
      methodType = io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
  public static io.grpc.MethodDescriptor<RecommendOuterClass.UploadMusicRequest,
      RecommendOuterClass.UploadMusicReply> getUploadMusicMethod() {
    io.grpc.MethodDescriptor<RecommendOuterClass.UploadMusicRequest, RecommendOuterClass.UploadMusicReply> getUploadMusicMethod;
    if ((getUploadMusicMethod = RecommendGrpc.getUploadMusicMethod) == null) {
      synchronized (RecommendGrpc.class) {
        if ((getUploadMusicMethod = RecommendGrpc.getUploadMusicMethod) == null) {
          RecommendGrpc.getUploadMusicMethod = getUploadMusicMethod =
              io.grpc.MethodDescriptor.<RecommendOuterClass.UploadMusicRequest, RecommendOuterClass.UploadMusicReply>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "UploadMusic"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  RecommendOuterClass.UploadMusicRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  RecommendOuterClass.UploadMusicReply.getDefaultInstance()))
              .setSchemaDescriptor(new RecommendMethodDescriptorSupplier("UploadMusic"))
              .build();
        }
      }
    }
    return getUploadMusicMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static RecommendStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<RecommendStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<RecommendStub>() {
        @Override
        public RecommendStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new RecommendStub(channel, callOptions);
        }
      };
    return RecommendStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static RecommendBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<RecommendBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<RecommendBlockingStub>() {
        @Override
        public RecommendBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new RecommendBlockingStub(channel, callOptions);
        }
      };
    return RecommendBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static RecommendFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<RecommendFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<RecommendFutureStub>() {
        @Override
        public RecommendFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new RecommendFutureStub(channel, callOptions);
        }
      };
    return RecommendFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   *ip: 43.138.152.89:9090
   * The service definition.
   * </pre>
   */
  public interface AsyncService {

    /**
     */
    default void getMusic(RecommendOuterClass.RecommendRequest request,
                          io.grpc.stub.StreamObserver<RecommendOuterClass.RecommendReply> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetMusicMethod(), responseObserver);
    }

    /**
     */
    default io.grpc.stub.StreamObserver<RecommendOuterClass.UploadMusicRequest> uploadMusic(
        io.grpc.stub.StreamObserver<RecommendOuterClass.UploadMusicReply> responseObserver) {
      return io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall(getUploadMusicMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service Recommend.
   * <pre>
   *ip: 43.138.152.89:9090
   * The service definition.
   * </pre>
   */
  public static abstract class RecommendImplBase
      implements io.grpc.BindableService, AsyncService {

    @Override public final io.grpc.ServerServiceDefinition bindService() {
      return RecommendGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service Recommend.
   * <pre>
   *ip: 43.138.152.89:9090
   * The service definition.
   * </pre>
   */
  public static final class RecommendStub
      extends io.grpc.stub.AbstractAsyncStub<RecommendStub> {
    private RecommendStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected RecommendStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new RecommendStub(channel, callOptions);
    }

    /**
     */
    public void getMusic(RecommendOuterClass.RecommendRequest request,
                         io.grpc.stub.StreamObserver<RecommendOuterClass.RecommendReply> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetMusicMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<RecommendOuterClass.UploadMusicRequest> uploadMusic(
        io.grpc.stub.StreamObserver<RecommendOuterClass.UploadMusicReply> responseObserver) {
      return io.grpc.stub.ClientCalls.asyncClientStreamingCall(
          getChannel().newCall(getUploadMusicMethod(), getCallOptions()), responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service Recommend.
   * <pre>
   *ip: 43.138.152.89:9090
   * The service definition.
   * </pre>
   */
  public static final class RecommendBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<RecommendBlockingStub> {
    private RecommendBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected RecommendBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new RecommendBlockingStub(channel, callOptions);
    }

    /**
     */
    public RecommendOuterClass.RecommendReply getMusic(RecommendOuterClass.RecommendRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetMusicMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service Recommend.
   * <pre>
   *ip: 43.138.152.89:9090
   * The service definition.
   * </pre>
   */
  public static final class RecommendFutureStub
      extends io.grpc.stub.AbstractFutureStub<RecommendFutureStub> {
    private RecommendFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected RecommendFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new RecommendFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<RecommendOuterClass.RecommendReply> getMusic(
        RecommendOuterClass.RecommendRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetMusicMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_GET_MUSIC = 0;
  private static final int METHODID_UPLOAD_MUSIC = 1;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_GET_MUSIC:
          serviceImpl.getMusic((RecommendOuterClass.RecommendRequest) request,
              (io.grpc.stub.StreamObserver<RecommendOuterClass.RecommendReply>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @Override
    @SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_UPLOAD_MUSIC:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.uploadMusic(
              (io.grpc.stub.StreamObserver<RecommendOuterClass.UploadMusicReply>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getGetMusicMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              RecommendOuterClass.RecommendRequest,
              RecommendOuterClass.RecommendReply>(
                service, METHODID_GET_MUSIC)))
        .addMethod(
          getUploadMusicMethod(),
          io.grpc.stub.ServerCalls.asyncClientStreamingCall(
            new MethodHandlers<
              RecommendOuterClass.UploadMusicRequest,
              RecommendOuterClass.UploadMusicReply>(
                service, METHODID_UPLOAD_MUSIC)))
        .build();
  }

  private static abstract class RecommendBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    RecommendBaseDescriptorSupplier() {}

    @Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return RecommendOuterClass.getDescriptor();
    }

    @Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("Recommend");
    }
  }

  private static final class RecommendFileDescriptorSupplier
      extends RecommendBaseDescriptorSupplier {
    RecommendFileDescriptorSupplier() {}
  }

  private static final class RecommendMethodDescriptorSupplier
      extends RecommendBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    RecommendMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (RecommendGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new RecommendFileDescriptorSupplier())
              .addMethod(getGetMusicMethod())
              .addMethod(getUploadMusicMethod())
              .build();
        }
      }
    }
    return result;
  }
}

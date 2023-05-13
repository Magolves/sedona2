set(VM_SRCS 
    datetimeStd_DateTimeServiceStd.c  
    main.c		      
    sys_Str.c
    inet_Crypto_sha1.c
    nativetable.c
    sys_Sys.c
    inet_sha1.c
    sys_Component.c	      
    sys_Sys_std.c
    inet_TcpServerSocket_std.c	  
    sys_File_std.c
    sys_Sys_unix.c
    inet_TcpSocket_std.c
    sys_FileStore_std.c
    sys_Test.c
    inet_UdpSocket_std.c
    sys_PlatformService_unix.c
    sys_Type.c
    inet_util_std.c
    sys_StdOutStream_std.c
    vm.c)

add_executable(svmt ${VM_SRCS})
target_compile_options(svmt PRIVATE -Wextra -Wno-unused-variable -Wno-unused-parameter -m32)
target_link_options(svmt PRIVATE -m32)
target_compile_definitions(svmt PRIVATE PLAT_BUILD_VERSION="1.2.29" __UNIX__=1 SOCKET_FAMILY_INET=1)

install(TARGETS svmt DESTINATION bin)
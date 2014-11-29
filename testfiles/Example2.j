.source Type1.j
.class public Example3
.super java/lang/Object

.method public <init>()V
	aload_0
	invokenonvirtual java/lang/Object/<init>()V
	return
.end method

.method public static main([Ljava/lang/String;)V
	.limit stack 2
	
	getstatic java/lang/System/out Ljava/io/PrintStream;
	ldc "42"

	label1:
		goto label2
	label2:
		goto label3
	label3: 
		goto label4	
	label4:
		invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V
	return
.end method
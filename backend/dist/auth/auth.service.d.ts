import { PrismaService } from '../prisma/prisma.service';
export declare class AuthService {
    private prisma;
    constructor(prisma: PrismaService);
    register(registerDto: any): Promise<{
        message: string;
        applicantId: string;
    }>;
    login(loginDto: any): Promise<{
        message: string;
        token: string;
        applicantId: string | undefined;
    }>;
    logout(): Promise<{
        message: string;
    }>;
}

import { PrismaService } from '../prisma/prisma.service';
export declare class AuthService {
    private prisma;
    constructor(prisma: PrismaService);
    register(registerDto: any): Promise<{
        message: string;
        applicantId: any;
    }>;
    login(loginDto: any): Promise<{
        message: string;
        token: string;
        applicantId: any;
    }>;
    logout(): Promise<{
        message: string;
    }>;
}

import { PrismaService } from '../prisma/prisma.service';
export declare class WebhooksService {
    private prisma;
    constructor(prisma: PrismaService);
    processConsultantAction(payload: any): Promise<{
        status: string;
    }>;
}

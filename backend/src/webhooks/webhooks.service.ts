import { Injectable } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';

@Injectable()
export class WebhooksService {
  constructor(private prisma: PrismaService) {}

  async processConsultantAction(payload: any) {
    const { applicantId, consultantId, action, comment } = payload;
    
    console.log(`[Webhook] Processing action '${action}' from consultant ${consultantId} for applicant ${applicantId}`);

    // 1. Record the action
    await this.prisma.consultantAction.create({
      data: {
        applicantId,
        consultantId,
        action,
        comment
      }
    });

    // 2. Perform the action
    switch (action) {
      case 'verify_document':
        // Handle document verification
        if (payload.documentId) {
          await this.prisma.document.update({
            where: { id: payload.documentId },
            data: { verificationStatus: 'verified' }
          });
        }
        break;
      
      case 'request_information':
        // Trigger Journey Agent to ask applicant for missing info
        await this.prisma.notification.create({
          data: {
            applicantId,
            title: 'Action Required',
            message: `Consultant requested: ${comment}`
          }
        });
        
        await this.prisma.applicant.update({
          where: { id: applicantId },
          data: { journeyStatus: 'info_requested' }
        });
        break;
        
      default:
        console.warn(`[Webhook] Unhandled action type: ${action}`);
    }

    return { status: 'success' };
  }
}

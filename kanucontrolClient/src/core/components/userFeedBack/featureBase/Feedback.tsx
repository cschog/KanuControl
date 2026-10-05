import { MessageCircle } from "lucide-react";

export function FeedbackButton() {
  return (
    <button className="fixed bottom-6 right-6 rounded-full shadow-xl p-4 bg-blue-600 text-white">
      <MessageCircle />
    </button>
  );
}
